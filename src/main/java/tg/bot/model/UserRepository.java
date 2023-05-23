package tg.bot.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findAllByTime(int time);

    @Modifying
    @Transactional
    @Query("update User u set u.time= :time where u.chatId= :chatId")
    void setTime(long chatId, int time);
}
