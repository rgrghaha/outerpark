package outerpark;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel="musicals", path="musicals")
public interface MusicalRepository extends PagingAndSortingRepository<Musical, Long>{

    Musical findByMusicalId(Long MusicalId);

}
