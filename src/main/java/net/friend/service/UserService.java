package net.friend.service;

import net.friend.model.CustomUserDetails;
import net.friend.model.User;
import net.friend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {

  @Autowired
  UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {

    User user = userRepository.findByUsername(userName);

    if (user == null) throw new UsernameNotFoundException(userName);

    return new CustomUserDetails(user);
  }

  public UserDetails loadUserById(Long id) {

    User user = userRepository.findById(id);

    if(user==null) return null;

    return new CustomUserDetails(user);
  }
}
