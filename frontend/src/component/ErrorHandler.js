import React, { useState } from "react";
import { useSubscription } from "react-stomp-hooks";

const ErrorHandler = () => {

  const [errorState, setErrorState] = useState({
     code: "", message: "",
  });

  const onErrorMessageReceived = (payload) => {
    console.log("error received", payload);
    let payloadData = JSON.parse(payload.body);
    
    setErrorState({ ...errorState, code: payloadData.code, message: payloadData.message })
  };
  useSubscription("/user/queue/error", onErrorMessageReceived);

  return (<></>);
}

export default ErrorHandler