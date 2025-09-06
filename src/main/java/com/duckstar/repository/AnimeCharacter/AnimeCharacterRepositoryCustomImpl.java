package com.duckstar.repository.AnimeCharacter;

import com.duckstar.domain.QCharacter;
import com.duckstar.domain.mapping.QAnimeCharacter;
import com.duckstar.web.dto.AnimeResponseDto;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class AnimeCharacterRepositoryCustomImpl implements AnimeCharacterRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QAnimeCharacter animeCharacter = QAnimeCharacter.animeCharacter;
    private final QCharacter character = QCharacter.character;

    @Override
    public List<AnimeResponseDto.CastPreviewDto> getAllCharacterHomePreviewsByAnimeId(Long animeId) {
        return queryFactory.select(
                        Projections.constructor(AnimeResponseDto.CastPreviewDto.class,
                                character.mainThumbnailUrl,
                                character.nameKor,
                                character.cv
                        )
                ).from(animeCharacter)
                .join(animeCharacter.character, character)
                .where(animeCharacter.anime.id.eq(animeId))
                .orderBy(character.id.asc())
                .fetch();
    }
}
