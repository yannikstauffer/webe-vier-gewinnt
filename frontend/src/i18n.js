import i18n from "i18next";
import { initReactI18next } from "react-i18next";
import LanguageDetector from 'i18next-browser-languagedetector';
import Backend from 'i18next-http-backend';

i18n
    .use(Backend) // provide messages from /public/locales
    .use(LanguageDetector) // detect user language
    .use(initReactI18next) // i18n object to be initialized
    .init({
        detection: { order: ["path", "navigator"] },
        fallbackLng: "de",
        whitelist: ['de', 'en'],
        debug: true,
        interpolation: {
            escapeValue: false // react already handles this by default
        }
    });

export default i18n;