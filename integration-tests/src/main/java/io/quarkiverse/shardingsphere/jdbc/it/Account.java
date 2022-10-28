package io.quarkiverse.shardingsphere.jdbc.it;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@Entity
@Table(name = "t_account")
@NamedQuery(name = "findAll", query = "SELECT a FROM Account a ORDER BY account_id")
public class Account {
    @Id
    private int account_id;
    private int user_id;
    private String status;

    public Account(int account_id, int user_id, String status) {
        this.account_id = account_id;
        this.user_id = user_id;
        this.status = status;
    }

    public Account() {

    }

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

    @Override
    public String toString() {
        return "Account [account_id=" + account_id + ", status=" + status + ", user_id=" + user_id + "]";
    }
}
