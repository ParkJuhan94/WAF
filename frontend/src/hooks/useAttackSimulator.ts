import { useState, useCallback } from 'react';
import { AttackTestService, AttackTestResult } from '../services/attackTests';
import { ATTACK_SCENARIOS } from '../utils/constants';

const attackTestService = new AttackTestService();

export const useAttackSimulator = () => {
  const [isRunning, setIsRunning] = useState(false);
  const [results, setResults] = useState<AttackTestResult[]>([]);
  const [progress, setProgress] = useState({ current: 0, total: 0 });

  const runSingleTest = useCallback(async (
    attackType: keyof typeof ATTACK_SCENARIOS,
    targetUrl: string
  ): Promise<AttackTestResult> => {
    setIsRunning(true);
    try {
      const result = await attackTestService.runSingleTest(attackType, targetUrl);
      setResults(prev => [...prev, result]);
      return result;
    } catch (error) {
      console.error('Attack test failed:', error);
      throw error;
    } finally {
      setIsRunning(false);
    }
  }, []);

  const runAllTests = useCallback(async (targetUrl: string): Promise<AttackTestResult[]> => {
    setIsRunning(true);
    setResults([]);

    const testTypes = Object.keys(ATTACK_SCENARIOS) as Array<keyof typeof ATTACK_SCENARIOS>;
    setProgress({ current: 0, total: testTypes.length });

    const allResults: AttackTestResult[] = [];

    try {
      for (let i = 0; i < testTypes.length; i++) {
        const testType = testTypes[i];
        setProgress({ current: i + 1, total: testTypes.length });

        try {
          const result = await attackTestService.runSingleTest(testType, targetUrl);
          allResults.push(result);
          setResults(prev => [...prev, result]);
        } catch (error) {
          console.error(`Test ${testType} failed:`, error);
          // Continue with other tests
        }

        // Add delay between tests
        if (i < testTypes.length - 1) {
          await new Promise(resolve => setTimeout(resolve, 1000));
        }
      }

      return allResults;
    } finally {
      setIsRunning(false);
      setProgress({ current: 0, total: 0 });
    }
  }, []);

  const runDVWAComplianceTest = useCallback(async () => {
    setIsRunning(true);
    try {
      const result = await attackTestService.runDVWAComplianceTest();
      setResults(result.attackTests);
      return result;
    } catch (error) {
      console.error('DVWA compliance test failed:', error);
      throw error;
    } finally {
      setIsRunning(false);
    }
  }, []);

  const generateReport = useCallback(async (testResults?: AttackTestResult[]): Promise<Blob> => {
    const resultsToUse = testResults || results;
    return attackTestService.generateComplianceReport(resultsToUse);
  }, [results]);

  const validateResults = useCallback((testResults?: AttackTestResult[]) => {
    const resultsToUse = testResults || results;
    return attackTestService.validateTestResults(resultsToUse);
  }, [results]);

  const clearResults = useCallback(() => {
    setResults([]);
    setProgress({ current: 0, total: 0 });
  }, []);

  const getTestSummary = useCallback(() => {
    if (results.length === 0) {
      return {
        total: 0,
        blocked: 0,
        failed: 0,
        blockRate: 0
      };
    }

    const blocked = results.filter(r => r.blocked).length;
    const failed = results.length - blocked;
    const blockRate = (blocked / results.length) * 100;

    return {
      total: results.length,
      blocked,
      failed,
      blockRate
    };
  }, [results]);

  return {
    // State
    isRunning,
    results,
    progress,

    // Actions
    runSingleTest,
    runAllTests,
    runDVWAComplianceTest,
    generateReport,
    validateResults,
    clearResults,

    // Computed
    summary: getTestSummary(),

    // Helpers
    getAttackScenarios: () => ATTACK_SCENARIOS
  };
};