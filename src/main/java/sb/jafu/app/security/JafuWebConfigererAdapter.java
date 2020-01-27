package sb.jafu.app.security;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;

/**
 * @author SAROY on 1/24/2020
 */
public class JafuWebConfigererAdapter extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.
                csrf().disable()
                .httpBasic().disable()
                .formLogin().disable()
                .anonymous().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .exceptionHandling().authenticationEntryPoint(new Http403ForbiddenEntryPoint());

        http.authorizeRequests().regexMatchers("/user/.*").hasAnyAuthority("admin", "user");
    }
}
