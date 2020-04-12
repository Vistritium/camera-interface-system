import React, {useState, useEffect, FormEventHandler, FormEvent} from 'react';

import "./Gcode.scss"
import * as Server from "../Server"
import {faCopy} from "@fortawesome/free-solid-svg-icons";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
// @ts-ignore
import {CopyToClipboard} from 'react-copy-to-clipboard';

export const Gcode = () => {

    const [failure, setFailure] = useState<string | undefined>();
    const [result, setResult] = useState<string | undefined>();
    const [clipboardCopy, setClipboardCopy] = useState<boolean>(false);
    const [showTextInput, setShowTextInput] = useState<boolean>(true);

    const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
        setClipboardCopy(false)
        event.preventDefault();
        // @ts-ignore
        const formData = new FormData(event.target)
        const fetchData = async () => {
            try {
                const result = await fetch(Server.address("/gcode/parse"), {
                    method: 'POST',
                    body: formData
                })
                const resultData = await result.text()
                console.log(resultData)
                setResult(resultData)
                setFailure(undefined)
                setShowTextInput(false)
            } catch (e) {
                setFailure("Error: " + e.toString())
            }
        }
        fetchData()
    }
    /* method="post" action={Server.address("/gcode/parse")} encType="multipart/form-data" */

    return <div>

        {failure ? <div className="gcode">
            <div className="alert alert-danger" role="alert">
                {failure}
            </div>
            <br/>
        </div> : null}
        <div className="gcode">
            <form onSubmit={handleSubmit}>
                <div className="gcode-inner">
                    <div className='gcode-button gcode-inline'>
                        <label htmlFor="file">Choose file to upload</label>
                        <input type="file" id="file" name="file"/>
                        <div>
                            <button>Submit</button>
                        </div>
                    </div>
                    <div className="gcode-inline gcode-button-container">
                        <button type="button" className="btn btn-primary"
                                onClick={() => setShowTextInput(!showTextInput)}>{showTextInput ? "Ukryj" : "Pokaż"} obszar
                            wejściowy
                        </button>
                    </div>
                    <textarea className={showTextInput ? "gcode-inline" : "gcode-text-hidden"} name="text" rows={100} cols={200}/>
                    {result ?
                        <div className="gcode-copy gcode-inline">
                            <CopyToClipboard text={result} onCopy={() => setClipboardCopy(true)}>
                                <FontAwesomeIcon size="3x" icon={faCopy}/>
                            </CopyToClipboard>
                            {clipboardCopy ? <div className="alert alert-success" role="alert">
                                Copied to clipboard
                            </div> : null}
                        </div> : null}
                    {result ? <div className="gcode-result">
                        <text>
                            {
                                result.split('\n').map((item, i) => {
                                        return <div key={i}>{item}</div>;
                                    }
                                )
                            }</text>
                    </div> : null}

                </div>

            </form>
        </div>
    </div>
};