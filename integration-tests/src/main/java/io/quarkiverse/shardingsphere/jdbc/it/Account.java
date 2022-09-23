package io.quarkiverse.shardingsphere.jdbc.it;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import io.quarkus.runtime.annotations.RegisterForReflection;

@Entity
@Table(name = "t_account")
@ApplicationScoped
@RegisterForReflection
public class Account {
    @Id
    private int account_id;
    private int user_id;
    private String status;

    public int getAccount_id() {
        return account_id;
    }

    public void setAccount_id(int account_id) {
        this.account_id = account_id;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
