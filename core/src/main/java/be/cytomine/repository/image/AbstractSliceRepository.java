package be.cytomine.repository.image;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import be.cytomine.domain.image.AbstractImage;
import be.cytomine.domain.image.AbstractSlice;
import be.cytomine.domain.image.UploadedFile;

/**
 * Spring Data JPA repository for the abstract image entity.
 */
@Repository
public interface AbstractSliceRepository
    extends JpaRepository<AbstractSlice, Long>, JpaSpecificationExecutor<AbstractSlice> {

    List<AbstractSlice> findAllByImage(AbstractImage abstractImage);

    List<AbstractSlice> findAllByUploadedFile(UploadedFile uploadedFile);

    @Query(
        value =
            "SELECT asl "
                + "FROM AbstractSlice asl "
                + "WHERE asl.image = :image "
                + "AND asl.channel = :channel "
                + "AND asl.zStack = :zStack "
                + "AND asl.time = :time"
    )
    Optional<AbstractSlice> findByImageAndChannelAndZStackAndTime(
        AbstractImage image,
        Integer channel,
        Integer zStack,
        Integer time
    );

    List<AbstractSlice> findAllByImageAndChannel(AbstractImage image, int index);
}
