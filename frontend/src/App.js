import React, {useState, useEffect} from "react";
import Lobby from "./component/Lobby";
import Chat from "./component/Chat";
import Game from "./component/Game";
import Banner from "./component/Banner";
import {StompSessionProvider} from "react-stomp-hooks";
import {BrowserRouter as Router, Routes, Route} from "react-router-dom";
import {useTranslation} from "react-i18next";
import './i18n';
import './App.css';

const BASE_URL = "http://localhost:9000";
const VIERGEWINNT_URL = BASE_URL + "/4gewinnt";
const WS_URL = VIERGEWINNT_URL + "/ws";

function App() {
    const {t, i18n} = useTranslation();

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
        <StompSessionProvider
            url={WS_URL}
            connectHeaders={csrfHeaders}
            debug={(str) => console.log(str)}
        >
            <Banner/>
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
                        <Route path="/" element={<Lobby userId={userId}/>}/>
                        <Route path="/game/:gameId" element={<Game userId={userId}/>}/>
                        <Route path="/lobby" element={<Lobby userId={userId}/>}/>
                    </Routes>
                    <Chat userId={userId}/>
                </div>
            </Router>
        </StompSessionProvider>
    );
}

export default App;
