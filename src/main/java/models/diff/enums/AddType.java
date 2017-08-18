package models.diff.enums;

/**
 * Created by salizumberi-laptop on 03.12.2016.
 */
public enum AddType implements ChangeType<AddType>{
    ADD,
    CMD,
    COMMENT,
    COPY,
    ENTRYPOINT,
    EXECUTABLE_PARAMETER,
    ENV,
    EXPOSE,
    FROM,
    LABEL,
    ONBUILD,
    RUN,
        STOPSIGNAL,
    USER,
    VOLUME,
    WORKDIR,
    SOURCE,
    DESTINATION,
    ARG,
    EXECUTABLE,
    PARAMETER,
    KEY,
    VALUE,
    PORT,
    IMAGE,
    IMAGE_NAME,
    IMAGE_VERSION_STRING,
    IMAGE_VERSION_NUMBER,
    IMAGE_VERSION_DIGEST,
    OPTION_PARAMETER,
    MAINTAINER,
    SIGNAL,
    USER_NAME,
    PATH,
    HEALTHCHECK;

    AddType valueOf(){
        return valueOf(name());
    }
}
