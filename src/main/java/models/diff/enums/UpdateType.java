package models.diff.enums;

/**
 * Created by salizumberi-laptop on 03.12.2016.
 */
public enum UpdateType implements ChangeType<UpdateType>{
    ADD,
    CMD,
    COPY,
    ENTRYPOINT,
    ENV,
    EXPOSE,
    COMMENT,
    FROM,
    LABEL,
    MAINAINER,
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
    EXECUTABLE_PARAMETER,
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

    UpdateType valueOf(){
        return valueOf(name());
    }
}
