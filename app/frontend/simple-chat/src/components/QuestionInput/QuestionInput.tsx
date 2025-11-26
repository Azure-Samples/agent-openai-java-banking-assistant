import { useState, useRef } from "react";
import { Stack, TextField } from "@fluentui/react";
import { Button, Tooltip,Textarea } from "@fluentui/react-components";
import { Send28Filled, Attach24Filled, Delete16Filled } from "@fluentui/react-icons";
import { QuestionContextType } from "./QuestionContext";
import { uploadAttachment } from "../../api";

import styles from "./QuestionInput.module.css";

interface Props {
    onSend: (questionContext: QuestionContextType) => void;
    disabled: boolean;
    placeholder?: string;
    clearOnSend?: boolean;
}

export const  QuestionInput = ({ onSend, disabled, placeholder, clearOnSend }: Props) => {
    const [question, setQuestion] = useState<string>("");
    const inputFile = useRef<HTMLInputElement | null>(null);
    const [attachmentRef, setAttachmentRef] = useState<File | null>(null);
    const [previewImage, setPreviewImage] = useState<string | null>(null);
    const [isUploading, setIsUploading] = useState<boolean>(false);


    const internalSendQuestion = async() => {
        const questionContext = {
            question: question,
            attachments: attachmentRef != null ? [attachmentRef.name] : []
            };

            onSend(questionContext);

            if (clearOnSend) {
                setQuestion("");
            }

            setAttachmentRef(null);
            setPreviewImage(null);
    }

    const sendQuestion = async() => {
        if (disabled || !question.trim()) {
            return;
        }

        if( attachmentRef != null){
           
           setIsUploading(true); 
           console.log("Uploading file... "+ attachmentRef.name);
           //await uploadAttachment(attachmentRef);
         
           uploadAttachment(attachmentRef)
            .then((response) => {
                console.log("File uploaded.");
                setIsUploading(false);
                internalSendQuestion()
                })
            .catch((error) => {
               console.error(error);
               setIsUploading(false);
           });
        } else {
            internalSendQuestion()
        }
        
        
    };

    const onEnterPress = (ev: React.KeyboardEvent<Element>) => {
        if (ev.key === "Enter" && !ev.shiftKey) {
            ev.preventDefault();
            sendQuestion();
        }
    };

    const onQuestionChange = (_ev: React.FormEvent<HTMLInputElement | HTMLTextAreaElement>, newValue?: string) => {
        if (!newValue) {
            setQuestion("");
        } else if (newValue.length <= 1000) {
            setQuestion(newValue);
        }
    };

    const onAttach = (_ev : React.MouseEvent<HTMLButtonElement>) => {
        inputFile.current?.click();
    }

    const onFileSelected = (_ev : React.ChangeEvent<HTMLInputElement>) => {
        if (_ev.target.files) {
            setAttachmentRef(_ev.target.files[0]);
            setPreviewImage(URL.createObjectURL(_ev.target.files[0]));
        }

    }
    
    const onAttachDelete = (_ev : React.MouseEvent<HTMLButtonElement>) => {
       setAttachmentRef(null);
       setPreviewImage(null);
    }
    const sendQuestionDisabled = disabled || !question.trim();

    return (
        <div>
        <Stack horizontal className={styles.questionInputContainer}>
             {previewImage && (
                    
                    <div className={styles.attachmentContainer}>
                    <img className={styles.imagePreview} src={previewImage} alt="" />
                    <Button size="small" icon={<Delete16Filled primaryFill="rgba(115, 118, 225, 1)" />} onClick={onAttachDelete}  />
                        { isUploading && (<p>Uploading File...</p>)}
                   </div>
                )}
            <TextField
                className={styles.questionInputTextArea}
                placeholder={placeholder}
                multiline
                resizable={false}
                borderless
                value={question}
                onChange={onQuestionChange}
                onKeyDown={onEnterPress}
            />
             <div className={styles.questionInputButtonsContainer}>
                <Tooltip content="Attachement" relationship="label">
                    <Button size="large" icon={<Attach24Filled primaryFill="rgba(115, 118, 225, 1)" />}  onClick={onAttach} />
                </Tooltip>
                <input type='file' id='file' ref={inputFile} style={{display: 'none'}} onChange={onFileSelected} accept="image/png, image/jpeg"/>
            </div>
            <div className={styles.questionInputButtonsContainer}>
                <Tooltip content="Ask question button" relationship="label">
                    <Button size="large" icon={<Send28Filled primaryFill="rgba(115, 118, 225, 1)" />} disabled={sendQuestionDisabled} onClick={sendQuestion} />
                </Tooltip>
            </div>
           
        </Stack>
        
       </div>
    );
};
