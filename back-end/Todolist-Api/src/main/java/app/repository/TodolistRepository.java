package app.repository;

import app.entity.Todolist;
import app.entity.User;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface TodolistRepository extends JpaRepository<Todolist, String> {
    boolean existsByTodolist(String todolist);

    boolean existsByTodolistAndUser(String todolist, User user);

    void deleteAllByUser(User user);

    Optional<Todolist> findByIdAndUser(String id,User user);

    List<Todolist> findAllByUser(User user);

    List<Todolist> findAllByUser(User user, Sort sort);
}
