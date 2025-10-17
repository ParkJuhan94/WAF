package dev.waf.console.user.repository;

import dev.waf.console.common.fixture.TestFixtures;
import dev.waf.console.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UserRepository 테스트
 *
 * TestFixtures를 사용한 레포지토리 테스트 예시
 */
@DataJpaTest
@DisplayName("User Repository 테스트")
class UserRepositoryTest implements TestFixtures {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("이메일로 사용자 조회 - 성공")
    void findByEmail_Success() {
        // given: TestFixture를 사용하여 테스트 데이터 생성
        User user = users().createDefaultUser();
        entityManager.persistAndFlush(user);

        // when: 이메일로 사용자 조회
        Optional<User> foundUser = userRepository.findByEmail(user.getEmail());

        // then: 조회 결과 검증
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo(user.getEmail());
        assertThat(foundUser.get().getName()).isEqualTo(user.getName());
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 조회 - 실패")
    void findByEmail_NotFound() {
        // when: 존재하지 않는 이메일로 조회
        Optional<User> foundUser = userRepository.findByEmail("nonexistent@example.com");

        // then: 조회 결과가 없어야 함
        assertThat(foundUser).isEmpty();
    }

    @Test
    @DisplayName("Provider와 ProviderId로 사용자 조회 - 성공")
    void findByProviderAndProviderId_Success() {
        // given: 특정 provider 사용자 생성
        User user = users().createUser(
            "google-user@example.com",
            "Google User",
            "google",
            "google_12345"
        );
        entityManager.persistAndFlush(user);

        // when: provider와 providerId로 조회
        Optional<User> foundUser = userRepository.findByProviderAndProviderId("google", "google_12345");

        // then: 조회 결과 검증
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getProvider()).isEqualTo("google");
        assertThat(foundUser.get().getProviderId()).isEqualTo("google_12345");
    }

    @Test
    @DisplayName("여러 사용자 저장 및 조회")
    void saveMultipleUsers() {
        // given: 여러 사용자 생성
        User[] users = users().createMultipleUsers(5);

        // when: 사용자들 저장
        for (User user : users) {
            entityManager.persist(user);
        }
        entityManager.flush();

        // then: 모든 사용자가 저장되었는지 확인
        long count = userRepository.count();
        assertThat(count).isEqualTo(5);
    }

    @Test
    @DisplayName("사용자 프로필 업데이트")
    void updateUserProfile() {
        // given: 사용자 생성 및 저장
        User user = users().createDefaultUser();
        entityManager.persistAndFlush(user);

        Long userId = user.getId();
        String newName = "Updated Name";
        String newProfileImage = "https://example.com/new-profile.jpg";

        // when: 프로필 업데이트
        user.updateProfile(newName, newProfileImage);
        entityManager.flush();
        entityManager.clear();

        // then: 업데이트된 정보 확인
        User updatedUser = userRepository.findById(userId).orElseThrow();
        assertThat(updatedUser.getName()).isEqualTo(newName);
        assertThat(updatedUser.getProfileImage()).isEqualTo(newProfileImage);
    }

    @Test
    @DisplayName("마지막 로그인 시간 업데이트")
    void updateLastLogin() {
        // given: 사용자 생성 및 저장
        User user = users().createDefaultUser();
        entityManager.persistAndFlush(user);

        Long userId = user.getId();

        // when: 마지막 로그인 시간 업데이트
        user.updateLastLogin();
        entityManager.flush();

        // then: 업데이트 확인
        User updatedUser = userRepository.findById(userId).orElseThrow();
        assertThat(updatedUser.getLastLoginAt()).isNotNull();
    }
}
