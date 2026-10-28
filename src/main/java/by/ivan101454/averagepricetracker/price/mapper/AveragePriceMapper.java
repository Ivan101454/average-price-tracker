package by.ivan101454.averagepricetracker.price.mapper;

import by.ivan101454.averagepricetracker.price.dto.PriceResponse;
import by.ivan101454.averagepricetracker.price.entity.Price;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface AveragePriceMapper {

    AveragePriceMapper INSTANCE = Mappers.getMapper(AveragePriceMapper.class);

    PriceResponse toResponse(Price entity);

}
