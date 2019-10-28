package net.friend.repository;

import net.friend.model.User;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface UserRepository extends PagingAndSortingRepository<User, Integer>, JpaSpecificationExecutor<User> {

    User findById(long id);

    User findByUsername(String userName);

}
