package org.test.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.data.domain.Page;
import org.test.dto.response.PageResponseDto;
import org.test.dto.response.UserResponseDto;
import org.test.dto.response.UserSearchResponseDto;
import org.test.model.EmailData;
import org.test.model.PhoneData;
import org.test.model.User;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "balance", source = "account.balance")
    @Mapping(target = "emails", source = "emails", qualifiedByName = "mapEmails")
    @Mapping(target = "phones", source = "phones", qualifiedByName = "mapPhones")
    UserResponseDto toUserResponseDto(User user);

    @Mapping(target = "emails", source = "emails", qualifiedByName = "mapEmails")
    @Mapping(target = "phones", source = "phones", qualifiedByName = "mapPhones")
    UserSearchResponseDto toUserSearchResponseDto(User user);

    @Named("mapEmails")
    default List<String> mapEmails(List<EmailData> emails) {
        if (emails == null) {
            return Collections.emptyList();
        }
        return emails.stream()
                .map(EmailData::getEmail)
                .collect(Collectors.toList());
    }

    @Named("mapPhones")
    default List<String> mapPhones(List<PhoneData> phones) {
        if (phones == null) {
            return Collections.emptyList();
        }
        return phones.stream()
                .map(PhoneData::getPhone)
                .collect(Collectors.toList());
    }

    default PageResponseDto<UserSearchResponseDto> toPageResponseDto(Page<User> page) {
        List<UserSearchResponseDto> content = page.getContent().stream()
                .map(this::toUserSearchResponseDto)
                .collect(Collectors.toList());

        return new PageResponseDto<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }
}
