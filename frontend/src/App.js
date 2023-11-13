import React, { useState, useEffect } from "react";
import Lobby from "./component/Lobby";
import Chat from "./component/Chat";
import Game from "./component/Game";
import Banner from "./component/Banner";
import { StompSessionProvider } from "react-stomp-hooks";
import { createUseStyles, ThemeProvider } from "react-jss";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import { useTranslation } from "react-i18next";
import './i18n';

const BASE_URL = "http://localhost:9000";
const VIERGEWINNT_URL = BASE_URL + "/4gewinnt";
const WS_URL = VIERGEWINNT_URL + "/ws";

const theme = {
  accentBackgroundColor: "rgb(240 240 240)",
  accentColor: "rgb(30 30 30)",
  accentHighlightBackgroundColor: "rgb(200 200 200)",
  accentHighlightColor: "rgb(10 10 10)",
  errorBackgroundColor: "rgb(246,189,189)",
  errorTextColor: "rgb(171,0,0)",
  infoBackgroundColor: "rgb(178,212,252)",
  infoTextColor: "rgb(0,52,131)",
};

const buttonBase = (theme) => ({
  fontFamily: "inherit",
  fontSize: "inherit",
  textDecoration: "none",
  padding: "0 1em",
  mozPaddingStart: "calc(10px - 3px)",
  margin: "4px 0",
  height: "40px",
  boxShadow: "1px 1px 2px 0.1px rgb(0 0 0 / 30%)",
  border: "none",
  borderRadius: "3px",
  cursor: "pointer",
  lineHeight: "1.1",
  textAlign: "center",
  display: "flex",
  flexGrow: "0",
  alignItems: "center",
  justifyContent: "center",
  color: theme.accentColor,
  backgroundColor: theme.accentBackgroundColor,

  "&:hover": {
    backgroundColor: theme.accentHighlightBackgroundColor,
    color: theme.accentHighlightColor,
    cursor: "pointer",
  },
});

const textInputBase = {
  fontFamily: "inherit",
  fontSize: "inherit",
  backgroundColor: "transparent",
  padding: "0 1em 0 0.5em",
  mozPaddingStart: "calc(10px - 3px)",
  margin: "4px 0",
  height: "40px",
  boxShadow: "2px 2px 5px 1px rgb(0 0 0 / 30%)",
  border: "none",
  borderRadius: "3px",
  lineHeight: "1.1",
};

const useStyles = createUseStyles(theme=> ({
  "@global": {
    body: {
      margin: "0",
      fontFamily:
        "-apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', 'Oxygen',\n    'Ubuntu', 'Cantarell', 'Fira Sans', 'Droid Sans', 'Helvetica Neue',\n    sans-serif",
      webkitFontSmoothing: "antialiased",
      mozOsxFontSmoothing: "grayscale",
      height: "100vh",
    },
    html: {
      height: "100%",
    },
    "#root": {
      position: "absolute",
      display: "flex",
      flexFlow: "column nowrap",
      top: 0,
      bottom: 0,
      left: 0,
      right: 0,
      overflowY: "hidden",
    },

    button: {
      extend: buttonBase(theme),
    },

    '.button': {
      extend: buttonBase(theme),
    },

    input: {
      '[type="text"], [type="password"], [type="number"]': {
        extend: textInputBase,
      },
      '[type="submit"], [type="reset"], [type="button"]': {
        extend: buttonBase(theme),
      },
    },
    select: {
      extend: textInputBase,
    },
    textarea: {
      extend: textInputBase,
      paddingTop: "0.6em",
      width: "100%",
    },
    ".flex-row": {
      display: "flex",
      flexFlow: "row nowrap",
      justifyContent: "center",
      alignItems: "center",
      gap: "5px",
    },
    ".layout": {
      display: "grid",
      gridTemplateColumns: "2fr 1fr",
      gridTemplateRows: "min-content auto",
      margin: "0 auto",
      height: "100%",
      gap: "5px",
    },
  },
}));

function App() {
  useStyles(theme);
  const { t, i18n } = useTranslation();

  const [userId, setUserId] = useState(Math.floor(Math.random() * 1000));
  const [isLoading, setLoading] = useState(true);
  const [csrfHeaders, setCsrfHeaders] = useState({});

  useEffect(() => {
    loadCsrfStompHeaders().then((headers) => {
      console.log("CSRF headers loaded", headers);
      setCsrfHeaders(headers);
      setLoading(false);
    });
  }, []);

  useEffect(() => {
    fetch(VIERGEWINNT_URL + "/currentUserId")
      .then((response) => {
        if (!response.ok) {
          throw new Error("Network response was not ok");
        }
        return response.json();
      })
      .then((data) => {
        console.log("Current user id:", data);
        setUserId(data);
      })
      .catch((error) => {
        console.error("There was a problem with the fetch operation:", error);
      });
  }, []);

  const loadCsrfStompHeaders = () => {
    return fetch(VIERGEWINNT_URL + "/csrf")
      .then((response) => {
        if (response.ok) {
          return response.json();
        } else {
          throw new Error("Failed to fetch CSRF token");
        }
      })
      .then((csrfDetails) => {
        return {
          "X-CSRF-TOKEN": csrfDetails.token,
        };
      });
  };
  const changeLanguage = (language) => {
    i18n.changeLanguage(language);
  };

  return isLoading ? (
    <div>{t('app.loading')}</div>
  ) : (
    <ThemeProvider theme={theme}>
      <StompSessionProvider
        url={WS_URL}
        connectHeaders={csrfHeaders}
        debug={(str) => console.log(str)}
      >
        <Banner />
        <Router>
          <div className="layout">
            <div>
              <h1>{t('app.title')}</h1>
            </div>
            <div className="flex-row">
              <button className="button" onClick={() => changeLanguage('de')}>
                DE
              </button>
              <button className="button" onClick={() => changeLanguage('en')}>
                EN
              </button>
                <a className="button" href={BASE_URL + "/logout"}>
                  {t('app.action.logout')}
              </a>
            </div>
            <Routes>
              <Route path="/" element={<Lobby userId={userId} />} />
              <Route path="/game/:gameId" element={<Game userId={userId} />} />
              <Route path="/lobby" element={<Lobby userId={userId} />} />
            </Routes>
            <Chat userId={userId} />
          </div>
        </Router>
      </StompSessionProvider>
    </ThemeProvider>
  );
}

export default App;
