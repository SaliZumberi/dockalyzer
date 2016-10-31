package dockalyzer.app;

import java.io.Serializable;

import javax.persistence.*;

@Entity
@Table(name = "student")
public class Student implements Serializable {

    @Id
    @Column(name = "df_id", columnDefinition = "serial")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_name", nullable = false)
    private String name;

    @Column(name = "student_age", nullable = false)
    private int age;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    @Override
    public String toString() {
        return id + "\t" + name + "\t" + age;
    }
}