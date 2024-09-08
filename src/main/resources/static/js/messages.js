import * as main from "./modules/main.mjs"
import * as auth from "./modules/auth.mjs";
import {getUsername} from "./modules/user.mjs";

main.loadHeader()
main.loadFooter()

const NO_MESSAGES = "Geen berichten."
const PREFIX_FROM = "Van : "
const PREFIX_TO = "Aan : "
const ASK_CONFIRMATION_FOR_DELETE = "Weet u zeker dat u dit bericht wilt verwijderen?"
const CONFIRMATION_FOR_DELETE = "Bericht verwijderd."
const CONFIRMATION_FOR_UPDATE = "Bericht bijgewerkt."
const BUTTON_TEXT_CONFIRM = "Akkoord"
const BUTTON_TEXT_CANCEL = "Annuleren"

const NOTIFICATION_DURATION = 3000
const DATE_TIME_OPTIONS = {
    weekday: `long`,
    year: `numeric`,
    month: `long`,
    day: `numeric`,
    hour: `numeric`,
    minute: `numeric`
}
let loggedInUser = {}
let inboxArray = {}
let outboxArray = {}
let selectedMessage = {}
let sortAscending = false
let overviewShowsInbox = true

// authenticate user
const token = auth.getToken()
await auth.checkIfLoggedIn(token)

setup()

async function setup() {
    document.querySelector('#notification').style.display = `none`

    document.querySelector('#refreshInboxButton').addEventListener('click', async () => {
        console.log("refreshinbox event fired")
        // TODO extract this to a new function, combined with the one below
        overviewShowsInbox = true
        inboxArray = await getInbox()
        if (inboxArray.length > 0) {
            sortMessageArray(inboxArray)
            fillMessageOverview(inboxArray)
            await showMessageContent(inboxArray[0].messageId)
            showElement(`messageSingleView`, true)
        } else noMessages()
    })

    document.querySelector('#refreshOutboxButton').addEventListener('click', async () => {
        console.log("refreshoutbox event fired")
        // TODO extract this to a new function
        overviewShowsInbox = false
        outboxArray = await getOutbox()
        if (outboxArray.length > 0) {
            sortMessageArray(outboxArray)
            fillMessageOverview(outboxArray)
            await showMessageContent(outboxArray[0].messageId)
            showElement(`messageSingleView`, true)
        } else noMessages()
    })

    document.querySelector('#reverseMessageOverviewButton').addEventListener('click', () => {
        reverseMessageOverview()
    })

    document.querySelector('#overviewVisibilityButton').addEventListener('click', () => {
        showNotification("overviewVisibilityButton clicked!")
    })

    document.querySelector('#writeMessageButton').addEventListener('click', () => {
        window.location.href = "send-a-message.html"
    })

    document.querySelector('#deleteMessageButton').addEventListener('click', () => {
        showDeleteMessageDialog()
        selectedMessage = null
        if (overviewShowsInbox) {
            // inboxArray.find(element => element.messageId === Number(messageId))
            // inboxArray.splice(... , ... ) voor verwijderen van element met bekende index
            document.querySelector('#refreshInboxButton').click()
            console.log("refreshinbox.click() inside DeleteMessageButton function")
        } else {
            document.querySelector('#refreshOutboxButton').click()
            console.log("refreshoutbox.click() inside DeleteMessageButton function")
        }
    })


    loggedInUser = await auth.getLoggedInUser(token)

    // refresh messageoverview with inbox by default
    document.querySelector('#refreshInboxButton').click();
}

export function showNotification(message) {
    let notification = document.querySelector('#notification')
    notification.innerHTML = message
    notification.style.display = `block`
    setTimeout(() => {
        notification.style.display = `none`;
    }, NOTIFICATION_DURATION);

}

// // for floatingCheatMenu
// document.querySelector('#fillListOfCorrespondentsButton').addEventListener('click', () => {
//     fillCorrespondentsDropDown(listOfCorrespondents, "receiverDropDown")
// })
// document.querySelector('#fillReceiverDropDown').addEventListener('click', () => {
//     fillCorrespondentsDropDown(listOfCorrespondents, "receiverDropDown")
// })

async function getInbox() {
    overviewShowsInbox = true
    inboxArray = await getMessages('in')
    if (inboxArray === undefined) {
        noMessages()
    } else {
        return inboxArray
    }
}

async function getOutbox() {
    overviewShowsInbox = false
    outboxArray = await getMessages('out')
    if (outboxArray === undefined) {
        noMessages()
    } else {
        return outboxArray
    }
}

// returns fetched messages of inbox or outbox
async function getMessages(box) {
    let boxParameter = {}
    if (box != null) {
        boxParameter = `?box=` + box
    } else boxParameter = ``
    const url = `/api/messages${boxParameter}`
    try {
        const response = await fetch(url, {
            headers: {
                "Authorization": localStorage.getItem('authToken')
            }
        })
        if (!response.ok) {
            new Error(`Response status: ${response.status}`)
        } else if (response.status === 200) {
            return await response.json()
        } else if (response.status === 204) {
            return [];
        }
    } catch
        (error) {
        console.error(error.message);
    }
}

async function getMessage(messageId) {
    const url = `/api/messages/${messageId}`
    try {
        const response = await fetch(url, {
            headers: {
                "Authorization": localStorage.getItem('authToken')
            }
        })
        if (!response.ok) {
            new Error(`Response status: ${response.status}`)
        } else {
            return response.json()
        }
    } catch
        (error) {
        console.error(error.message);
    }
}

// sort the array according to sortAscending value (newest on top, or oldest on top)
function sortMessageArray(array) {
    if (array.length !== 0) {
        if (sortAscending) {
            array.sort((a, b) => a.dateTimeSent.localeCompare(b.dateTimeSent))
        } else {
            array.sort((a, b) => b.dateTimeSent.localeCompare(a.dateTimeSent))
        }
    }
}

function fillMessageOverview(listOfMessages) {
    // remove old messages from overview
    document.querySelectorAll(`#messageInOverview`).forEach(e => e.remove())
    // build new element for every message in the list, and add it to messageOverview
    if (listOfMessages != null) {
        listOfMessages.forEach(element => {
            // create new message element
            const newOverviewMessage = document.createElement("div");
            newOverviewMessage.setAttribute("id", "messageInOverview")
            newOverviewMessage.setAttribute("data-messageid", `${element.messageId}`)
            // add eventhandler to entire element
            newOverviewMessage.addEventListener('click', () => {
                showMessageContent(`${element.messageId}`)
                console.log("element.readByReceiver is ")
                console.log(element.readByReceiver)
                if (overviewShowsInbox && element.readByReceiver === false) {
                    element.readByReceiver = true
                    console.log("element.readByReceiver is ")
                    console.log(element.readByReceiver)
                    updateMessage(element)
                }
            })

            // add subject as child
            const subject = document.createElement(`div`)
            subject.setAttribute("class", "subject")
            subject.textContent = element.subject
            newOverviewMessage.appendChild(subject)

            // add senderId, dateTimeSent and messageId as child
            const senderAndDateTime = document.createElement(`div`)
            const senderId = element.senderId
            const dateTimeSent = formatDateTime(element.dateTimeSent)
            const messageIdElement = element.messageId
            senderAndDateTime.textContent = `${senderId}, ${dateTimeSent}, ${messageIdElement}`
            newOverviewMessage.appendChild(senderAndDateTime)

            // add newOverviewRow to the overview
            document.querySelector(`#messageOverview`).appendChild(newOverviewMessage)
        })
    } else {
        noMessages()
    }
}

function noMessages() {
    document.querySelectorAll(`#messageInOverview`).forEach(e => e.remove())
    let noMessages = document.createElement(`div`)
    noMessages.setAttribute("id", "messageInOverview")
    noMessages.textContent = NO_MESSAGES
    document.querySelector(`#messageOverview`).appendChild(noMessages)
    showElement(`messageSingleView`, false)
}

function showElement(elementSelector, boolean) {
    const element = document.getElementById(`${elementSelector}`);
    if (boolean) {
        element.style.display = "block";
    } else {
        element.style.display = "none";
    }
}

// async function updateMessageProperty(messageId, key, value) {
//     let message
//     if (overviewShowsInbox) {
//         message = inboxArray.find(element => element.messageId === Number(messageId))
//     } else {
//         message = outboxArray.find(element => element.messageId === Number(messageId))
//     }
//     message[key] = value
//     console.log("updateMessageProperty message is : ")
//     console.log(message)
//     await updateMessage(message)
// }

async function updateMessage(message) {
    const url = `/api/messages`
    try {
        const response = await fetch(url, {
            method: "PUT",
            headers: {
                "Authorization": localStorage.getItem('authToken'),
                "Content-Type": "application/json"
            },
            body: JSON.stringify(message)
        })
        if (!response.ok) {
            new Error(`Response status: ${response.status}`)
        } else {
            // TODO notification OK
            // console.log("updateMessage success for messageId " + message.messageId)
        }
    } catch (error) {
        console.error(error.message);
    }
}

async function deleteMessage(message) {
    const url = `/api/messages`
    try {
        const response = await fetch(url, {
            method: "DELETE",
            headers: {
                "Authorization": localStorage.getItem('authToken'),
                "Content-Type": "application/json"
            },
            body: JSON.stringify(message)
        })
        if (!response.ok) {
            new Error(`Response status: ${response.status}`)
        } else {
            // console.log("deleteMessage success for messageId " + message.messageId)
            // TODO add notification for user
            await getOutbox()
        }
    } catch (error) {
        // TODO add notification for user
        console.error(error.message);
    }
}

function showDeleteMessageDialog() {
    let dialog = document.querySelector("dialog")
    let dialogText = document.querySelector("#dialogText")
    let dialogButtonConfirm = document.querySelector("#dialogButtonConfirm")
    let dialogButtonCancel = document.querySelector("#dialogButtonCancel")
    dialogText.innerHTML = ASK_CONFIRMATION_FOR_DELETE
    dialogButtonConfirm.innerHTML = BUTTON_TEXT_CONFIRM
    dialogButtonConfirm.addEventListener("click", () => {
        deleteMessageHelper(selectedMessage)
        dialog.close();
    });
    dialogButtonCancel.innerHTML = BUTTON_TEXT_CANCEL
    dialogButtonCancel.addEventListener("click", () => {
        dialog.close();
    });
    document.querySelector("dialog").showModal()


}

async function deleteMessageHelper(message) {
    console.log("deleteMessageHelper is called")
    if (message) {
        if (loggedInUser.userId === message.senderId) {
            // sender deletes message; message is deleted from database
            await deleteMessage(message)
        } else {
            // receiver deletes message; message is marked "archivedByReceiver" using update function
            message.archivedByReceiver = true
            await updateMessage(message)
        }
        showNotification(CONFIRMATION_FOR_DELETE)
    } else {
        // TODO notification "Select a message to delete"
    }
}

function reverseMessageOverview() {
    // flip boolean value of sortAscending
    sortAscending = !sortAscending
    let listview = document.querySelector("#messageOverview")
    for (let i = 1; i < listview.childNodes.length; i++) {
        listview.insertBefore(listview.childNodes[i], listview.firstChild)
    }
}

async function showMessageContent(messageId) {
    // check which overview is showing
    let visibleArray = overviewShowsInbox ? inboxArray : outboxArray
    // find the message in the array, using its messageId
    selectedMessage = visibleArray.find((e) => e.messageId === parseInt(messageId, 10))
    // show the message values in the relevant HTML elements
    document.querySelector(`#singleViewUsername`).textContent = overviewShowsInbox ?
        PREFIX_FROM + await getUsername(selectedMessage.senderId) + ", "
        : PREFIX_TO + await getUsername(selectedMessage.receiverId) + ", "
    const messageDateTime = new Date(selectedMessage.dateTimeSent)
    document.querySelector(`#singleViewDateTimeSent`).textContent = formatDateTime(messageDateTime)
    document.querySelector(`#singleViewSubject`).textContent = selectedMessage.subject
    document.querySelector(`#singleViewBody`).textContent = selectedMessage.body
}

async function getMessageById(messageId) {
    const url = `/api/messages/${messageId}`
    try {
        const response = await fetch(url)
        if (!response.ok) {
            new Error(`Response status: ${response.status}`)
        }
        return await response.json()
    } catch (error) {
        console.error(error.message)
    }
}


function getMessageIdFromInputField() {
    return document.querySelector("#messageIdInput").value
}

// format date syntax according to browser's language setting
function formatDateTime(dateTimeSent) {
    let dateTime = new Date(dateTimeSent)
    return dateTime.toLocaleDateString(undefined, DATE_TIME_OPTIONS)
}