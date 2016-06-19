package com.github.dbunit.rules;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Calendar;

import com.github.dbunit.rules.util.EntityManagerProvider;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.github.dbunit.rules.api.dataset.DataSet;
import com.github.dbunit.rules.model.Tweet;

/**
 * Created by pestano on 15/02/16.
 */
public class ScriptReplacementsIt {

    Calendar now;

    @Rule
    public EntityManagerProvider emProvider = EntityManagerProvider.instance("rules-it");

    @Rule
    public DBUnitRule dbUnitRule = DBUnitRule.instance("rules-it",emProvider.connection());

    @Before
    public void setup(){
        now = Calendar.getInstance();
    }

    @Test
    @DataSet(value = "datasets/yml/js-with-date-replacements.yml",cleanBefore = true ,disableConstraints = true, executorId = "rules-it")
    public void shouldReplaceDateUsingJavaScriptInDataset() {
        Tweet tweet = (Tweet) emProvider.em().createQuery("select t from Tweet t where t.id = '1'").getSingleResult();
        assertThat(tweet).isNotNull();
        assertThat(tweet.getDate().get(Calendar.DAY_OF_MONTH)).isEqualTo(now.get(Calendar.DAY_OF_MONTH));
        assertThat(tweet.getDate().get(Calendar.HOUR_OF_DAY)).isEqualTo(now.get(Calendar.HOUR_OF_DAY));
    }

// tag::javascript-likes[]
    @Test
    @DataSet(value = "datasets/yml/js-with-calc-replacements.yml",cleanBefore = true ,disableConstraints = true, executorId = "rules-it")
    public void shouldReplaceLikesUsingJavaScriptInDataset() {
        Tweet tweet = (Tweet) emProvider.em().createQuery("select t from Tweet t where t.id = '1'").getSingleResult();
        assertThat(tweet).isNotNull();
        assertThat(tweet.getLikes()).isEqualTo(50);
    }
// end::javascript-likes[]


// tag::groovy[]
    @Test
    @DataSet(value = "datasets/yml/groovy-with-date-replacements.yml",cleanBefore = true, disableConstraints = true, executorId = "rules-it")
    public void shouldReplaceDateUsingGroovyInDataset() {
        Tweet tweet = (Tweet) emProvider.em().createQuery("select t from Tweet t where t.id = '1'").getSingleResult();
        assertThat(tweet).isNotNull();
        assertThat(tweet.getDate().get(Calendar.DAY_OF_MONTH)).isEqualTo(now.get(Calendar.DAY_OF_MONTH));
        assertThat(tweet.getDate().get(Calendar.HOUR_OF_DAY)).isEqualTo(now.get(Calendar.HOUR_OF_DAY));
    }
// end::groovy[]


}
