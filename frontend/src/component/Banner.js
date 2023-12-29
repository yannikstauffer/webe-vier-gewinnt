import React, {useState} from "react";
import {useSubscription} from "react-stomp-hooks";
import {useTranslation} from "react-i18next";
import './Banner.css';

const Banner = () => {
    const {t} = useTranslation();

    const [errorState, setErrorState] = useState({
        code: "", messageKey: "",
        icon: "❗",
        labelKey: "error.label.anErrorOccurred",
        className: "banner error-banner",
        closeCallback: () => resetErrorState(),
    });

    const [systemMessage, setSystemMessage] = useState({
        code: "", messageKey: "",
        icon: "ℹ️ ",
        labelKey: "system.label.message",
        className: "banner info-banner",
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

    return (<div className="banner-container">
        {bannerOf(errorState)}
        {bannerOf(systemMessage)}
    </div>);
}

export default Banner