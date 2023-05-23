package tg.bot.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface LinksRepository extends JpaRepository<Links, Long> {
    @Modifying
    @Transactional
    @Query("DELETE Links l WHERE l.id= :id")
    void deleteByLinkId(Long id);
}
