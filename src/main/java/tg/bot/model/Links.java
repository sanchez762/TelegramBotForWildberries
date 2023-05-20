package tg.bot.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.URL;

@Entity
@Table(name = "linksTable")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Links {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

    @Column(name = "link")
    private String link;

    @ManyToOne
    private User user;
}
