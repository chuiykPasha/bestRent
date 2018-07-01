package rent.service;

import org.apache.lucene.search.Query;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rent.entities.Apartment;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Transactional
@Service
public class FullTextSearchService {
    @PersistenceContext
    private EntityManager entityManager;

    public List<Apartment> searchApartmentByLocation(String location) {
        FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
        QueryBuilder qb = fullTextEntityManager.getSearchFactory().buildQueryBuilder().forEntity(Apartment.class).get();

        Query luceneQuery = qb.keyword().onFields("location").matching(location).createQuery();

        javax.persistence.Query jpqQuery = fullTextEntityManager.createFullTextQuery(luceneQuery, Apartment.class);
        List<Apartment> apartments = jpqQuery.getResultList();
        return apartments;
    }
}
