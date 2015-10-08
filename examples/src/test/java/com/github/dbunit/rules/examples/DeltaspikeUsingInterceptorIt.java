package com.github.dbunit.rules.examples;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

import java.util.Comparator;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.example.jpadomain.Company;
import org.example.jpadomain.Contact;
import org.example.service.deltaspike.CompanyRepository;
import org.example.service.deltaspike.DeltaSpikeContactService;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.github.dbunit.rules.cdi.api.UsingDataSet;

/**
 * Created by pestano on 23/07/15.
 */

@RunWith(CdiTestRunner.class)
public class DeltaspikeUsingInterceptorIt {
    
    @Inject
    EntityManager entityManager;

    @Inject
    DeltaSpikeContactService contactService;

    @Inject
    CompanyRepository companyRepository;


    @Test
    @UsingDataSet(value = "datasets/contacts.yml")
    public void shouldQueryAllCompanies() {
        assertNotNull(contactService);
        assertThat(contactService.findCompanies()).hasSize(4);
    }

    @Test
    @UsingDataSet(value = "datasets/contacts.json")
    public void shouldQueryAllContactsUsingJsonDataSet() {
        assertThat(companyRepository.count()).isEqualTo(4);
    }

    @Test
    @UsingDataSet(value = "datasets/contacts.yml")
    public void shouldFindCompanyByName() {
        Company expectedCompany = new Company("Google");
        assertNotNull(companyRepository);
        assertThat(companyRepository.findByName("Google")).
                isNotNull().usingElementComparator(new Comparator<Company>() {
            @Override
            public int compare(Company o1, Company o2) {
                return o1.getName().compareTo(o2.getName());
            }
        }).contains(expectedCompany);
    }

    @Test
    @UsingDataSet(value = "datasets/contacts.yml")
    public void shouldCreateCompany() {
        assertThat(companyRepository.count()).isEqualTo(4);
        Company company = new Company("test company");
        beginTx();
        Company companyCreated = companyRepository.save(company);
        assertThat(companyCreated.id).isNotNull();
        commitTx();
        assertThat(companyRepository.count()).isEqualTo(5);
    }


    @Test
    @UsingDataSet(value = "datasets/contacts.yml")
    public void shouldCreateContact() {
        Company google = companyRepository.findByName("Google").get(0);
        assertThat(contactService.countByCompanyAndName(google, "rmpestano")).isEqualTo(0);
        Contact rmpestano = new Contact("rmpestano", null, "rmpestano@gmail.com", google);
        beginTx();//for now deltaspike @Transactional isn't helping
        contactService.save(rmpestano);
        commitTx();
        assertThat(rmpestano.id).isNotNull();
        assertThat(contactService.countByCompanyAndName(google, "rmpestano")).isEqualTo(1);
    }

    @Test
    @UsingDataSet(value = "datasets/contacts.yml")
    public void shouldDeleteContact() {
        Company pivotal = companyRepository.findByName("Pivotal").get(0);
        assertThat(contactService.countByCompanyAndName(pivotal, "Spring")).
                isEqualTo(1);
        Contact spring = contactService.findByCompanyAndName(pivotal, "Spring").get(0);
        beginTx();//for now deltaspike @Transactional isn't helping
        contactService.delete(spring);
        commitTx();
        assertThat(contactService.countByCompanyAndName(pivotal, "Spring")).
                isEqualTo(0);
    }


    private void beginTx() {
        entityManager.getTransaction().begin();
    }

    private void commitTx() {
        entityManager.getTransaction().commit();
    }
}
