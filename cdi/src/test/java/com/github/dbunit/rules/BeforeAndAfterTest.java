package com.github.dbunit.rules;

import com.github.dbunit.rules.api.dataset.SeedStrategy;
import com.github.dbunit.rules.cdi.api.UsingDataSet;
import com.github.dbunit.rules.model.User;
import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;


/**
 * Created by pestano on 09/10/15.
 */
@RunWith(CdiTestRunner.class)
public class BeforeAndAfterTest {

    @Inject
    EntityManager em;


    @Before
    public void init() {
        em.getTransaction().begin();
        em.createNativeQuery("DELETE FROM USER").executeUpdate();//delete users inserted in other tests
        for (int i=0;i<6;i++ ) {
            User u = new User();
            u.setName("user" + i);
            em.persist(u);
        }//create 6 users
        em.flush();
        em.getTransaction().commit();
        assertThat(listUsers()).isNotNull().hasSize(6);//we should have 6 users before test
    }

    @Test
    @UsingDataSet(value = "yml/users.yml", seedStrategy = SeedStrategy.INSERT,
              cleanBefore = true, cleanAfter = true
    )
    public void shouldClearDatabaseBeforeAndAfter() {
        //six users in @before must be deleted by cleanBefore
        assertThat(listUsers()).
                isNotNull().hasSize(2);//inserted by dbunit, see users.yml
    }

    public List<User> listUsers() {
        return em.createQuery("select u from User u").getResultList();
    }

    @After
    public void end() {//clean after must delete rows inserted by dbunit
        assertThat(listUsers()).isEmpty();
    }
}
