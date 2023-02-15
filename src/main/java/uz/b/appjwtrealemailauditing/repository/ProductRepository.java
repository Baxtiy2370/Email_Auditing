package uz.b.appjwtrealemailauditing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import uz.b.appjwtrealemailauditing.entity.Product;
@RepositoryRestResource(path = "product")
public interface ProductRepository extends JpaRepository<Product,Long> {
}
