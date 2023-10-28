import React, { useState } from "react";
import { useSubscription } from "react-stomp-hooks";
import {useTranslation} from "react-i18next";

const ErrorHandler = () => {
  const { t, i18n } = useTranslation();

  const [errorState, setErrorState] = useState({
     code: "", messageKey: "",
  });

  const onErrorMessageReceived = (payload) => {

    console.log("error received", payload);
    let payloadData = JSON.parse(payload.body);

    setErrorState({ ...errorState, code: payloadData.code, messageKey: payloadData.messageKey })
  };
  useSubscription("/user/queue/error", onErrorMessageReceived);

  return errorState.code === "" ?
  (<></>) :
  (<div>
    {t('error.label.anErrorOccurred')}: {t(errorState.messageKey)}
  </div>);
}

export default ErrorHandler