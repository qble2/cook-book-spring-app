package cookbook.repository;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import qble2.cookbook.user.UserRepository;
import qble2.cookbook.user.model.User;

/*
 * TestDatabaseAutoConfiguration$EmbeddedDataSourceBeanFactoryPostProcessor - Replacing 'dataSource'
 * DataSource bean with embedded version EmbeddedDatabaseFactory - Starting embedded database:
 * url='jdbc:h2:mem:b279a83b-ffc2-4ea0-a0ad-7c67787cfb11;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false',
 * username='sa'
 */
@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

  @Autowired
  private UserRepository userRepository; // underTest

  @BeforeEach
  void setUp() {
    userRepository.deleteAll();
  }

  // this is just an example, native repository methods do not have to be tested, only new custom
  // ones
  @Test
  void itShouldCreateNewUser() {
    // given
    User user =
        User.builder().firstName("john").lastName("wick").email("john.wick@xyz.com").build();

    // when
    User expectedUser = userRepository.save(user);

    // then
    assertThat(expectedUser.getId()).isNotNull();
  }

}
