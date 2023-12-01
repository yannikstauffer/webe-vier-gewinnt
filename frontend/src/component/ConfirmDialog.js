import React from 'react';
import {useTranslation} from 'react-i18next';
import './ConfirmDialog.css';

const ConfirmDialog = ({open, text, onClose, onConfirm}) => {
    const {t} = useTranslation();

    if (!open) return null;

    return (
        <div className="confirm-dialog-container">
            <div className="confirm-dialog flex-column">
                <div onClick={onClose} className="close-button">✕</div>

                <div className="confirm-dialog-text">
                    {text}
                </div>
                {onConfirm &&
                    <div className="confirm-dialog-buttons">
                        <button onClick={onConfirm}>{t('game.confirm.yes')}</button>
                        <button onClick={onClose}>{t('game.confirm.no')}</button>
                    </div>
                }
            </div>
        </div>
    );
};

export default ConfirmDialog;