package be.cytomine.service.image;

import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import be.cytomine.domain.image.AbstractImage;
import be.cytomine.domain.image.AbstractSlice;
import be.cytomine.domain.image.ImageInstance;
import be.cytomine.domain.image.SliceInstance;
import be.cytomine.dto.image.SliceCoordinate;
import be.cytomine.dto.image.SliceCoordinates;
import be.cytomine.exceptions.ObjectNotFoundException;
import be.cytomine.exceptions.WrongArgumentException;
import be.cytomine.repository.image.AbstractSliceRepository;
import be.cytomine.repository.image.SliceInstanceRepository;

@RequiredArgsConstructor
@Service
public class SliceCoordinatesService {

    private final AbstractSliceRepository abstractSliceRepository;

    private final SliceInstanceRepository sliceInstanceRepository;

    public SliceCoordinates getSliceCoordinates(AbstractImage image) {
        List<AbstractSlice> slices = abstractSliceRepository.findAllByImage(image);
        SliceCoordinates sliceCoordinates = new SliceCoordinates(
            slices.stream().map(AbstractSlice::getChannel).distinct().sorted().collect(Collectors.toList()),
            slices.stream().map(AbstractSlice::getZStack).distinct().sorted().collect(Collectors.toList()),
            slices.stream().map(AbstractSlice::getTime).distinct().sorted().collect(Collectors.toList())
        );
        return sliceCoordinates;
    }


    public SliceCoordinate getReferenceSliceCoordinate(AbstractImage image) {
        SliceCoordinates sliceCoordinates = getSliceCoordinates(image);
        if (sliceCoordinates.getChannels().isEmpty()) {
            throw new WrongArgumentException("Cannot retrieve reference slices for AbstractImage " + image.getId());
        }
        SliceCoordinate referenceSliceCoordinates = new SliceCoordinate(
            sliceCoordinates.getChannels().get((int) Math.floor(sliceCoordinates.getChannels().size() / 2)),
            sliceCoordinates.getZStacks().get((int) Math.floor(sliceCoordinates.getZStacks().size() / 2)),
            sliceCoordinates.getTimes().get((int) Math.floor(sliceCoordinates.getTimes().size() / 2))
        );
        return referenceSliceCoordinates;
    }

    public AbstractSlice getReferenceSlice(AbstractImage abstractImage) {
        SliceCoordinate sliceCoordinate = getReferenceSliceCoordinate(abstractImage);
        return abstractSliceRepository.findByImageAndChannelAndZStackAndTime(
                abstractImage,
                sliceCoordinate.getChannel(),
                sliceCoordinate.getZStack(),
                sliceCoordinate.getTime()
            )
            .orElseThrow(() -> new ObjectNotFoundException(
                "AbstractSlice",
                "image:"
                    + abstractImage.getId()
                    + ","
                    + sliceCoordinate.getChannel()
                    + ":"
                    + sliceCoordinate.getZStack()
                    + ":"
                    + sliceCoordinate.getTime()
            ));
    }

    public SliceInstance getReferenceSlice(ImageInstance imageInstance) {
        AbstractSlice abstractSlice = getReferenceSlice(imageInstance.getBaseImage());
        return sliceInstanceRepository.findByBaseSliceAndImage(abstractSlice, imageInstance).orElse(null);
    }
}
