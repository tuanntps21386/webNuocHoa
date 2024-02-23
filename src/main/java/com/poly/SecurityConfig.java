package com.poly;

import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.poly.entity.Account;
import com.poly.service.AccountService;



@Configuration
@EnableWebSecurity
//@EnableGlobalMethodSecurity(prePostEnabled=true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	@Autowired
	BCryptPasswordEncoder pe;

	@Autowired
	AccountService accountService;

	// Mã hóa mật khẩu
	@Bean
	public static BCryptPasswordEncoder getPasswordEncoder() {
		return new BCryptPasswordEncoder();
	}

	
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
	    auth.userDetailsService(usernameOrEmail -> { //được sử dụng để cung cấp một UserDetailsService tùy chỉnh, sử dụng trong quá trình xác thực.
	        try {
	            Account user = accountService.findByUsernameOrEmail(usernameOrEmail);

	            if (user == null) {
	                throw new UsernameNotFoundException(usernameOrEmail + " not found!");
	            }

	            String password = pe.encode(user.getPassword());
				/* String password = user.getPassword(); */
	            String[] roles = user.getAuthorities().stream().map(er -> er.getRole().getId())
	            		//thu thập các phần tử từ luồng vào một tập hợp cụ thể  sử dụng Collectors.toList() 
	            		//để thu thập các ID của vai trò vào một danh sách.
	                    .collect(Collectors.toList()).toArray(new String[0]);
                   // chuyển đổi danh sách thành một mảng.
	            //new String[0] chỉ định kiểu của mảng kết quả là String[].
	            return User.withUsername(usernameOrEmail).password(password).roles(roles).build();
	            //tạo một đối tượng User với tên người dùng (username), mật khẩu đã được mã hóa, và danh sách các quyền.
	        } catch (NoSuchElementException e) {
	            throw new UsernameNotFoundException(usernameOrEmail + " not found!");
	        }
	    });
	}

	// Cho phép truy xuất REST API từ bên ngoài (doman khác)
	@Override
	public void configure(WebSecurity web) throws Exception {
		web.ignoring().antMatchers(HttpMethod.OPTIONS, "/**");
	}

	// Phân quyền sử dụng và hình thức đăng nhập
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		// CSRF,CORS
		http.csrf().disable();

//		Phân quyền
		http.authorizeRequests()
		.antMatchers("/order/**").authenticated()
		.antMatchers("/admin/**").hasAnyRole("STAF", "DIRE")
		.antMatchers("/rest/authorities","/rest/accounts").hasRole("DIRE").anyRequest().permitAll();

		http.formLogin()
				// Khi gặp địa chỉ URL này
				.loginPage("/security/login/form")
				// Đưa vào địa chỉ này để xử lý URL
				.loginProcessingUrl("/security/login")
				// False để trở lại trang người dùng vừa yêu cầu
				// True sẽ tiếp tục quay trở lại địa chỉ URL của trang đăng nhập
				.defaultSuccessUrl("/security/login/success", false).failureUrl("/security/login/error");


		http.exceptionHandling().accessDeniedPage("/security/unauthoried");// error

		// Đăng xuất
		http.logout().logoutUrl("/security/logoff").logoutSuccessUrl("/security/logoff/success");
	}
}
