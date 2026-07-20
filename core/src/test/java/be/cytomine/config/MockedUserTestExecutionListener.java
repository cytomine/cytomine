package be.cytomine.config;

import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;

import be.cytomine.common.repository.http.UserHttpContract;
import be.cytomine.mapper.UserMapper;
import be.cytomine.repository.security.UserRepository;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class MockedUserTestExecutionListener implements TestExecutionListener, Ordered {

    @Override
    public void beforeTestMethod(TestContext testContext) {
        ApplicationContext context = testContext.getApplicationContext();
        UserHttpContract userHttpContract = context.getBean(UserHttpContract.class);
        UserRepository userRepository = context.getBean(UserRepository.class);
        UserMapper userMapper = context.getBean(UserMapper.class);

        when(userHttpContract.search(anyString())).thenAnswer(invocation ->
            userRepository.findByUsernameLikeIgnoreCase(invocation.getArgument(0)).map(userMapper::map));
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
