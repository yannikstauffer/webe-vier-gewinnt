import React, {useState} from "react";
import {useSubscription} from "react-stomp-hooks";
import {useTranslation} from "react-i18next";
import {createUseStyles, useTheme} from "react-jss";

const bannerBase = () => ({
    display: "flex",
    flexFlow: "row nowrap",
    justifyContent: "space-between",
    alignItems: "center",
    gap: "10px",
    padding: "10px",
    borderRadius: "5px",
    boxShadow: "2px 2px 5px 1px rgb(0 0 0 / 30%)",
    "& > .clickable": {
        cursor: "pointer",
    }
});

const useStyles = createUseStyles(theme => ({
    bannerContainer: {
        display: "flex",
        flexFlow: "column nowrap",
        justifyContent: "flex-start",
        alignItems: "center",
        gap: "10px",

        maxWidth: "80%",
        position: "absolute",
        left: "50%",
        transform: "translate(-50%, 10px)",
    },
    errorBanner: {
        extend: bannerBase(),
        backgroundColor: theme.errorBackgroundColor,
        color: theme.errorTextColor,
    },
    infoBanner: {
        extend: bannerBase(),
        backgroundColor: theme.infoBackgroundColor,
        color: theme.infoTextColor,
    },
}));

const Banner = () => {
    const {t, i18n} = useTranslation();
    const theme = useTheme();
    const classes = useStyles(theme);

    const [errorState, setErrorState] = useState({
        code: "", messageKey: "",
        icon: "❗",
        labelKey: "error.label.anErrorOccurred",
        className: classes.errorBanner,
        closeCallback: () => resetErrorState(),
    });

    const [systemMessage, setSystemMessage] = useState({
        code: "", messageKey: "",
        icon: "ℹ️ ",
        labelKey: "system.label.message",
        className: classes.infoBanner,
        closeCallback: () => resetSystemMessage(),
    });

    const onErrorMessageReceived = (payload) => {
        console.debug("Error received.", payload);
        let payloadData = JSON.parse(payload.body);

        setErrorState({...errorState, code: payloadData.code, messageKey: payloadData.messageKey})

        setTimeout(() => {
            errorState.closeCallback();
        }, 5000);
    };

    const resetErrorState = () => {
        setErrorState({...errorState, code: "", messageKey: ""})
    }
    const resetSystemMessage = () => {
        setSystemMessage({...systemMessage, code: "", messageKey: ""})
    }

    const onSystemMessageReceived = (payload) => {
        console.debug("System message received.", payload);
        let payloadData = JSON.parse(payload.body);

        setSystemMessage({...systemMessage, code: payloadData.code, messageKey: payloadData.messageKey})
    };

    useSubscription("/user/queue/error", onErrorMessageReceived);
    useSubscription("/topic/system", onSystemMessageReceived);

    const bannerOf = (bannerContent) => {
        if (bannerContent.code === "") return (<></>);

        const bannerText = bannerContent.icon + t(bannerContent.labelKey) + ": " + t(bannerContent.messageKey);
        return (<div className={bannerContent.className}>
            <span>{bannerText}</span>
            <div onClick={() => bannerContent.closeCallback()} className="clickable">✕</div>
        </div>);
    }

    return (<div className={classes.bannerContainer}>
        {bannerOf(errorState)}
        {bannerOf(systemMessage)}
    </div>);
}

export default Banner