import { ATTACK_SCENARIOS } from '../utils/constants';
import { apiClient } from './api';

export interface AttackTestResult {
  type: string;
  name: string;
  payload: string;
  blocked: boolean;
  statusCode: number;
  response: string;
  matchedRules: string[];
  timestamp: string;
  screenshot?: string;
}

export class AttackTestService {
  async runSingleTest(attackType: keyof typeof ATTACK_SCENARIOS, targetUrl: string): Promise<AttackTestResult> {
    const scenario = ATTACK_SCENARIOS[attackType];

    try {
      const result = await apiClient.simulateAttack(attackType, targetUrl, scenario.payload);

      return {
        type: attackType,
        name: scenario.name,
        payload: scenario.payload,
        blocked: result.blocked,
        statusCode: result.statusCode,
        response: result.response,
        matchedRules: result.matchedRules,
        timestamp: new Date().toISOString(),
      };
    } catch (error) {
      console.error(`Test failed for ${scenario.name}:`, error);
      throw error;
    }
  }

  async runAllTests(targetUrl: string): Promise<AttackTestResult[]> {
    const results: AttackTestResult[] = [];
    const testTypes = Object.keys(ATTACK_SCENARIOS) as Array<keyof typeof ATTACK_SCENARIOS>;

    for (const testType of testTypes) {
      try {
        const result = await this.runSingleTest(testType, targetUrl);
        results.push(result);

        // Add delay between tests to avoid overwhelming the target
        await this.delay(1000);
      } catch (error) {
        console.error(`Failed to run test ${testType}:`, error);
        // Continue with other tests even if one fails
      }
    }

    return results;
  }

  async runDVWAComplianceTest(): Promise<{
    normalRequests: { passed: number; total: number };
    attackTests: AttackTestResult[];
    compliance: {
      normalRequestsPassed: boolean;
      allAttacksBlocked: boolean;
      overallPassed: boolean;
    };
  }> {
    try {
      const dvwaResult = await apiClient.runDVWATest();

      // Convert API result to AttackTestResult format
      const attackTests: AttackTestResult[] = dvwaResult.attackTests.map(test => ({
        type: test.type,
        name: this.getAttackName(test.type),
        payload: this.getAttackPayload(test.type),
        blocked: test.blocked,
        statusCode: test.statusCode,
        response: '',
        matchedRules: [],
        timestamp: new Date().toISOString(),
        screenshot: test.screenshot
      }));

      const normalRequestsPassed = dvwaResult.normalRequests.passed === dvwaResult.normalRequests.total;
      const allAttacksBlocked = attackTests.every(test => test.blocked);

      return {
        normalRequests: dvwaResult.normalRequests,
        attackTests,
        compliance: {
          normalRequestsPassed,
          allAttacksBlocked,
          overallPassed: normalRequestsPassed && allAttacksBlocked
        }
      };
    } catch (error) {
      console.error('DVWA compliance test failed:', error);
      throw error;
    }
  }

  async generateComplianceReport(testResults: AttackTestResult[]): Promise<Blob> {
    try {
      return await apiClient.generateTestReport();
    } catch (error) {
      console.error('Failed to generate compliance report:', error);
      throw error;
    }
  }

  private getAttackName(type: string): string {
    const scenarioKey = type as keyof typeof ATTACK_SCENARIOS;
    return ATTACK_SCENARIOS[scenarioKey]?.name || type;
  }

  private getAttackPayload(type: string): string {
    const scenarioKey = type as keyof typeof ATTACK_SCENARIOS;
    return ATTACK_SCENARIOS[scenarioKey]?.payload || '';
  }

  private delay(ms: number): Promise<void> {
    return new Promise(resolve => setTimeout(resolve, ms));
  }

  validateTestResults(results: AttackTestResult[]): {
    passed: boolean;
    summary: {
      totalTests: number;
      blockedTests: number;
      failedTests: number;
      blockRate: number;
    };
    details: {
      passed: AttackTestResult[];
      failed: AttackTestResult[];
    };
  } {
    const blockedTests = results.filter(result => result.blocked);
    const failedTests = results.filter(result => !result.blocked);
    const blockRate = (blockedTests.length / results.length) * 100;

    return {
      passed: failedTests.length === 0,
      summary: {
        totalTests: results.length,
        blockedTests: blockedTests.length,
        failedTests: failedTests.length,
        blockRate
      },
      details: {
        passed: blockedTests,
        failed: failedTests
      }
    };
  }
}

export const attackTestService = new AttackTestService();