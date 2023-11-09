import React from 'react';
import { useTranslation } from 'react-i18next';

const ConfirmDialog = ({ open, onClose, onConfirm }) => {
    const { t } = useTranslation();

    if (!open) return null;

    return (
        <div className="confirm-dialog-backdrop">
            <div className="confirm-dialog">
                <p>{t('game.confirm.leave')}</p>
                <button onClick={onConfirm}>{t('game.confirm.yes')}</button>
                <button onClick={onClose}>{t('game.confirm.no')}</button>
            </div>
        </div>
    );
};

export default ConfirmDialog;