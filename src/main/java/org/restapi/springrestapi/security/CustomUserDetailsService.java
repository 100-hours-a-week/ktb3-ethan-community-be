package org.restapi.springrestapi.security;

import lombok.RequiredArgsConstructor;
import org.restapi.springrestapi.finder.UserFinder;
import org.restapi.springrestapi.model.User;
import org.restapi.springrestapi.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserFinder userFinder;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        User user = userFinder.findByEmailOrAuthThrow(email);

        return new CustomUserDetails(user);
    }
}
