import styles from "./UserChatMessage.module.css";
import { Stack} from "@fluentui/react";
import { AttachmentType } from "../AttachmentType";
import { getImage} from "../../api";

interface Props {
    message: string;
    attachments?: string[];
}

// <img src={URL.createObjectURL(attachment.file)} alt={attachment.name} className={styles.attachementPreview} />

export const UserChatMessage = ({message, attachments}: Props) => {
    return (
        <>        
        {attachments && (
                <>
                    {attachments.map((attachment, index) => (
                        <div key={index} >
                           <img src={getImage(attachment)} alt={attachment} className={styles.attachementPreview} />

                        </div>
                    ))}
                </>
            )}
        <div className={styles.container}>
         <div className={styles.message}>{message}</div>
        </div>
        </>
    );
};
