package ru.practicum.shareit.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Query("select c " +
            "from Comment as c " +
            "join c.item as i " +
            "where i.id in(select i.id " +
            "from i where i.ownerId = ?1)")
    List<Comment> findByOwnerId(Long ownerId);

    List<Comment> findByItemId(Long itemId);
}
