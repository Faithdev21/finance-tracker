package com.example.financetracker.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@Setter
@Getter
@Builder
@Table(name = "categories")
@NoArgsConstructor
@AllArgsConstructor
public class CategoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, name = "name")
    private String name;

    @Column(name = "type")
    private String type;

    @ManyToMany(mappedBy = "categories", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JsonBackReference
    private Set<UserEntity> users = new HashSet<>();

    public CategoryEntity(long id, String name, String type) {
    }

    public void addUser(UserEntity user) {
        if (user != null && !this.users.contains(user)) {
            this.users.add(user);
            user.addCategory(this); // Синхронизируем обратную связь
        }
    }

    public void removeUser(UserEntity user) {
        if (user != null && this.users.contains(user)) {
            this.users.remove(user);
            user.removeCategory(this);
        }
    }

    @Override
    public String toString() {
        return "CategoryEntity(id=" + id + ", name=" + name + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CategoryEntity that = (CategoryEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

