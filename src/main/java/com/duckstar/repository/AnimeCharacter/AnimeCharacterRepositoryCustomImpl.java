package com.duckstar.repository.AnimeCharacter;

import com.duckstar.domain.QCharacter;
import com.duckstar.domain.mapping.QAnimeCharacter;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.duckstar.web.dto.CharacterResponseDto.*;

@Repository
@RequiredArgsConstructor
public class AnimeCharacterRepositoryCustomImpl implements AnimeCharacterRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QAnimeCharacter animeCharacter = QAnimeCharacter.animeCharacter;
    private final QCharacter character = QCharacter.character;

    @Override
    public List<CharacterHomePreviewDto> getAllCharacterHomePreviewsByAnimeId(Long animeId) {
        return queryFactory.select(
                        Projections.constructor(CharacterHomePreviewDto.class,
                                character.mainThumbnailUrl,
                                character.nameKor,
                                character.cv
                        )
                ).from(animeCharacter)
                .join(character).on(character.id.eq(animeCharacter.character.id))
                .where(animeCharacter.anime.id.eq(animeId))
                .orderBy(character.id.asc())
                .fetch();
    }
}
