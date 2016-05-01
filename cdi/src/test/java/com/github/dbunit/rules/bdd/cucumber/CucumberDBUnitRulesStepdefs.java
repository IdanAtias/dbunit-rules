package com.github.dbunit.rules.bdd.cucumber;

import com.github.dbunit.rules.model.User;
import cucumber.api.PendingException;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by rafael-pestano on 09/10/2015.
 */
public class CucumberDBUnitRulesStepdefs {



    @Given("^The following feature$")
    public void The_following_feature(String docstring) throws Throwable {
        assertNotNull(docstring);
    }

    @And("^The following Cucumber test$")
    public void The_following_Cucumber_test(String docstring) throws Throwable {
      assertNotNull(docstring);
    }

    @When("^The following cucumber steps are executed$")
    public void The_following_cucumber_steps_are_executed(String docstring) throws Throwable {
        assertNotNull(docstring);
    }

    @Then("^The database should be seeded with the dataset content before step execution$")
    public void The_database_should_be_seeded_with_the_dataset_content_before_step_execution() throws Throwable {
        // Express the Regexp above with the code you wish you had
    }
}
