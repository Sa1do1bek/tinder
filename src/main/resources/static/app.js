// Global o'zgaruvchilar

let currentUser = null
let authToken = null
let currentCard = null
let allUsers = []
let currentChatUser = null
let verificationData = {}
let stompClient = null
let currentConversationId = null
let chatSubscription = null;

// Axios CDN orqali yuklangan
const axios = window.axios

// API base URL
const API_BASE_URL = "http://localhost:8081"

// Axios instance yaratish
const request = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        "Content-Type": "application/json",
    },
})

// Default avatar SVG (base64 encoded)
const DEFAULT_AVATAR_40 =
    "data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNDAiIGhlaWdodD0iNDAiIHZpZXdCb3g9IjAgMCA0MCA0MCIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KPHJlY3Qgd2lkdGg9IjQwIiBoZWlnaHQ9IjQwIiBmaWxsPSIjZmY0NDU4Ii8+Cjx0ZXh0IHg9IjIwIiB5PSIyNSIgZm9udC1mYW1pbHk9IkFyaWFsIiBmb250LXNpemU9IjE2IiBmaWxsPSJ3aGl0ZSIgdGV4dC1hbmNob3I9Im1pZGRsZSI+8J+RpDwvdGV4dD4KPHN2Zz4="

const DEFAULT_AVATAR_120 =
    "data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTIwIiBoZWlnaHQ9IjEyMCIgdmlld0JveD0iMCAwIDEyMCAxMjAiIGZpbGw9Im5vbmUiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwc3ZnIj4KPHJlY3Qgd2lkdGg9IjEyMCIgaGVpZ2h0PSIxMjAiIGZpbGw9IiNmZjQ0NTgiLz4KPHRleHQgeD0iNjAiIHk9IjcwIiBmb250LWZhbWlseT0iQXJpYWwiIGZvbnQtc2l6ZT0iNDgiIGZpbGw9IndoaXRlIiB0ZXh0LWFuY2hvcj0ibWlkZGxlIj7wn5GkPC90ZXh0Pgo8L3N2Zz4="

const DEFAULT_AVATAR_400 =
    "data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNDAwIiBoZWlnaHQ9IjMwMCIgdmlld0JveD0iMCAwIDQwMCAzMDAiIGZpbGw9Im5vbmUiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+CjxyZWN0IHdpZHRoPSI0MDAiIGhlaWdodD0iMzAwIiBmaWxsPSIjZmY0NDU4Ii8+Cjx0ZXh0IHg9IjIwMCIgeT0iMTcwIiBmb250LWZhbWlseT0iQXJpYWwiIGZvbnQtc2l6ZT0iNzIiIGZpbGw9IndoaXRlIiB0ZXh0LWFuY2hvcj0ibWlkZGxlIj7wn5GkPC90ZXh0Pgo8L3N2Zz4="

const DEFAULT_AVATAR_50 =
    "data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNTAiIGhlaWdodD0iNTAiIHZpZXdCb3g9IjAgMCA1MCA1MCIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KPHJlY3Qgd2lkdGg9IjUwIiBoZWlnaHQ9IjUwIiBmaWxsPSIjZmY0NDU4Ii8+Cjx0ZXh0IHg9IjI1IiB5PSIzMCIgZm9udC1mYW1pbHk9IkFyaWFsIiBmb250LXNpemU9IjIwIiBmaWxsPSJ3aGl0ZSIgdGV4dC1hbmNob3I9Im1pZGRsZSI+8J+RpDwvdGV4dD4KPHN2Zz4="

// Token bilan so'rov yuborish
function setAuthToken(token) {
    authToken = token
    if (token) {
        request.defaults.headers.common["Authorization"] = `Bearer ${token}`
        localStorage.setItem("token", token)
        console.log("Token o'rnatildi:", token.substring(0, 20) + "...")
    } else {
        delete request.defaults.headers.common["Authorization"]
        localStorage.removeItem("token")
        console.log("Token o'chirildi")
    }
}

// Debug uchun request interceptor
request.interceptors.request.use(
    (config) => {
        console.log("API so'rov:", config.method?.toUpperCase(), config.url)
        return config
    },
    (error) => {
        console.error("So'rov xatoligi:", error)
        return Promise.reject(error)
    },
)

// Debug uchun response interceptor
request.interceptors.response.use(
    (response) => {
        console.log("API javob:", response.status, response.config.url)
        return response
    },
    (error) => {
        console.error("Javob xatoligi:", error.response?.status, error.response?.data)
        if (error.response?.status === 401 || error.response?.status === 403) {
            console.log("Avtorizatsiya xatoligi - logout qilinyapti")
            logout()
        }
        return Promise.reject(error)
    },
)

// WebSocket ulanish
// WebSocket ulanish funksiyasi
function connectWebSocket() {
    if (!currentUser?.id) {
        console.log("Foydalanuvchi ID si yo'q, WebSocket ulanmaydi");
        return;
    }

    if (stompClient && stompClient.connected) {
        console.log("WebSocket allaqachon ulangan");
        return;
    }

    const socket = new SockJS(`${API_BASE_URL}/ws?token=${authToken}`);
    stompClient = Stomp.over(socket);

    stompClient.connect(
        {}, // bu bo‘sh bo‘ladi
        (frame) => {
            console.log("WebSocket muvaffaqiyatli ulandi");

            stompClient.subscribe(`/user/${currentUser.id}/queue/conversations`, (message) => {
                console.log("Yangi suhbat yangilandi:", message.body);
                loadUserConversations();
            });

            if (currentConversationId) {
                subscribeToChat(currentConversationId);
            }
        },
        (error) => {
            console.error("WebSocket ulanishida xatolik:", error);
            setTimeout(connectWebSocket, 5000);
        }
    );
}

function subscribeToChat(conversationId) {
    if (chatSubscription) {
        chatSubscription.unsubscribe();
    }

    chatSubscription = stompClient.subscribe(
        `/topic/chat/${conversationId}`,
        (message) => {
            const data = JSON.parse(message.body);
            console.log("Yangi xabar qabul qilindi:", data);

            // Xabarni chatga qo'shish (ham yuborilgan, ham qabul qilingan)
            addMessageToChat(data, data.senderId === currentUser.id);

            // Agar xabarni yuborgan foydalanuvchi hozirgi foydalanuvchi bo'lmasa
            if (data.senderId !== currentUser.id) {
                // Xabarni o'qilgan deb belgilash
                markMessageAsRead(data.id);
            }
        }
    );
    console.log(`Chatga obuna bo'ldi: /topic/chat/${conversationId}`);
}

// Xabarni yuborish funksiyasi
function sendMessage() {
    const messageInput = document.getElementById("messageInput");
    const messageText = messageInput.value.trim();

    if (!messageText || !currentChatUser || !currentConversationId) return;

    const messageDto = {
        senderId: currentUser.id,
        receiverId: currentChatUser.id,
        message: messageText,
        conversationId: currentConversationId
    };

    if (stompClient && stompClient.connected) {
        const headers = {
            'Authorization': `Bearer ${authToken}`
        };
        stompClient.send("/app/chat/send", headers, JSON.stringify(messageDto));
        messageInput.value = "";
    } else {
        console.error("WebSocket ulanmagan");
        showMessage("Xabar yuborishda xatolik. Qayta ulanmoqda...", "error");
        connectWebSocket();
    }
}

// Chatga obuna bo'lish funksiyasi


// WebSocket uzish
function disconnectWebSocket() {
    if (window.stompClient) {
        window.stompClient.disconnect()
        window.stompClient = null
        console.log("WebSocket uzildi")
    }
}

// Sahifalarni boshqarish
function showPage(pageId) {
    document.querySelectorAll(".page").forEach((page) => {
        page.classList.remove("active")
    })
    document.getElementById(pageId).classList.add("active")
}

function showMainPage(pageId) {
    document.querySelectorAll(".main-page").forEach((page) => {
        page.classList.remove("active")
    })
    document.getElementById(pageId).classList.add("active")

    // Navigation tugmalarini yangilash
    document.querySelectorAll(".nav-item").forEach((item) => {
        item.classList.remove("active")
    })
    if (event && event.target) {
        const navItem = event.target.closest(".nav-item")
        if (navItem) navItem.classList.add("active")
    }
}

// Loading ko'rsatish/yashirish
function showLoading() {
    document.getElementById("loading").classList.remove("hidden")
}

function hideLoading() {
    document.getElementById("loading").classList.add("hidden")
}

// Modal oynani ko'rsatish
function showModal(content) {
    document.getElementById("modalBody").innerHTML = content
    document.getElementById("modal").classList.add("active")
}

function closeModal() {
    document.getElementById("modal").classList.remove("active")
}

// Xabar ko'rsatish
function showMessage(message, type = "info") {
    const messageDiv = document.createElement("div")
    messageDiv.className = `message-toast ${type}`
    messageDiv.textContent = message
    messageDiv.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        padding: 1rem 2rem;
        background: ${type === "error" ? "#dc3545" : type === "success" ? "#28a745" : "#007bff"};
        color: white;
        border-radius: 10px;
        z-index: 5000;
        animation: slideIn 0.3s ease;
    `

    document.body.appendChild(messageDiv)

    setTimeout(() => {
        messageDiv.remove()
    }, 3000)
}

// Device ID generatsiya qilish
function generateDeviceId() {
    return "device_" + Math.random().toString(36).substr(2, 9) + "_" + Date.now()
}

// AUTH FUNCTIONS

// Login
document.getElementById("loginForm").addEventListener("submit", async (e) => {
    e.preventDefault()

    const email = document.getElementById("loginEmail").value
    const password = document.getElementById("loginPassword").value
    let deviceId = document.getElementById("loginDeviceId").value

    if (!deviceId) {
        deviceId = generateDeviceId()
        document.getElementById("loginDeviceId").value = deviceId
    }

    showLoading()

    try {
        console.log("Login urinishi:", {email, deviceId})

        const response = await request.post("/auths/loginByEmail", {
            email,
            password,
            deviceId,
        })

        hideLoading()
        console.log("Login javobi:", response.data)

        if (response.data.code === 0) {
            // Kod yuborildi, tasdiqlash kerak
            verificationData = {email, deviceId, type: "login"}
            showMessage("Tasdiqlash kodi yuborildi", "info")
            showPage("verifyPage")
        } else if (response.data.code === 1) {
            // Muvaffaqiyatli kirish
            authToken = response.data.data
            currentUser = response.data.meta?.user || {}

            console.log("Login muvaffaqiyatli:", {
                authToken: !!authToken,
                currentUser,
                hasUserId: !!currentUser.id,
            })

            setAuthToken(authToken)
            localStorage.setItem("authToken", authToken)
            localStorage.setItem("currentUser", JSON.stringify(currentUser))
            showMessage("Muvaffaqiyatli kirdingiz!", "success")
            showPage("mainApp")

            // WebSocket ulanish
            connectWebSocket()

            if (currentUser && currentUser.id) {
                console.log("User ID mavjud:", currentUser.id)
                setTimeout(() => {
                    loadUserProfile()
                    loadAllUsers()
                    loadUserConversations()
                }, 1000)
            }
        }
    } catch (error) {
        hideLoading()
        console.error("Login xatoligi:", error)
        showMessage(error.response?.data?.message || "Xatolik yuz berdi", "error")
    }
})

// Register
document.getElementById("registerForm").addEventListener("submit", async (e) => {
    e.preventDefault()

    const username = document.getElementById("regUsername").value
    const fullname = document.getElementById("regFullname").value
    const email = document.getElementById("regEmail").value
    const password = document.getElementById("regPassword").value
    let deviceId = document.getElementById("regDeviceId").value
    const role = document.getElementById("regRole").value

    if (!deviceId) {
        deviceId = generateDeviceId()
        document.getElementById("regDeviceId").value = deviceId
    }

    showLoading()

    try {
        const response = await request.post("/auths/registerByEmail", {
            username,
            fullname,
            email,
            password,
            deviceId,
            role,
        })

        hideLoading()

        if (response.data.status.value === 200) {
            verificationData = {
                email,
                password,
                username,
                deviceId,
                userId: response.data.meta.userId,
                type: "register",
            }

            showPage("verifyPage")
            setTimeout(() => {
                showMessage("Tasdiqlash kodi yuborildi", "info")
            }, 50)
        }
    } catch (error) {
        hideLoading()
        showMessage(error.response?.data?.message || "Xatolik yuz berdi", "error")
    }
})

// Verify
document.getElementById("verifyForm").addEventListener("submit", async (e) => {
    e.preventDefault()

    const code = document.getElementById("verifyCode").value

    showLoading()

    try {
        if (verificationData.type === "register") {
            const response = await request.post("/auths/verify", {
                email: verificationData.email,
                password: verificationData.password,
                username: verificationData.username,
                code,
                deviceId: verificationData.deviceId,
            })

            hideLoading()

            if (response.data.status.value === 200) {
                authToken = response.data.data.token
                currentUser = response.data.data.user || response.data.meta || {id: null}

                setAuthToken(authToken)
                localStorage.setItem("authToken", authToken)
                localStorage.setItem("currentUser", JSON.stringify(currentUser))
                showMessage("Ro'yxatdan muvaffaqiyatli o'tdingiz!", "success")
                showPage("mainApp")

                connectWebSocket()

                if (currentUser && currentUser.id) {
                    setTimeout(() => {
                        loadUserProfile()
                        loadAllUsers()
                        loadUserConversations()
                    }, 1000)
                }
            }
        } else if (verificationData.type === "login") {
            const response = await request.post("/auths/checkCodeByEmail", null, {
                params: {
                    email: verificationData.email,
                    code,
                    deviceId: verificationData.deviceId,
                },
            })

            hideLoading()

            if (response.data.status.value === 200) {
                authToken = response.data.data.token
                currentUser = response.data.data.user
                setAuthToken(authToken)
                localStorage.setItem("authToken", authToken)
                localStorage.setItem("currentUser", JSON.stringify(currentUser))
                showMessage("Muvaffaqiyatli kirdingiz!", "success")
                showPage("mainApp")

                connectWebSocket()

                setTimeout(() => {
                    loadUserProfile()
                    loadAllUsers()
                    loadUserConversations()
                }, 1000)
            }
        }
    } catch (error) {
        hideLoading()
        showMessage(error.response?.data?.message || "Xatolik yuz berdi", "error")
    }
})

// Kodni qayta yuborish
async function resendCode() {
    if (!verificationData.email || !verificationData.deviceId || !verificationData.userId) {
        showMessage("Ma'lumotlar to'liq emas", "error")
        return
    }

    showLoading()

    try {
        const response = await request.post("/auths/resendCodeToEmail", null, {
            params: {
                email: verificationData.email,
                deviceId: verificationData.deviceId,
                id: verificationData.userId,
            },
        })

        hideLoading()

        if (response.data.status.value === 200) {
            showMessage("Kod qayta yuborildi", "success")
        }
    } catch (error) {
        hideLoading()
        showMessage(error.response?.data?.message || "Xatolik yuz berdi", "error")
    }
}

// Google Auth
document.getElementById("googleAuthForm").addEventListener("submit", async (e) => {
    e.preventDefault()

    const email = document.getElementById("googleEmail").value
    const fullName = document.getElementById("googleFullName").value
    const picPath = document.getElementById("googlePicPath").value

    showLoading()

    try {
        const response = await request.post("/users/googleAuth", null, {
            params: {
                email,
                fullName,
                picPath,
            },
        })

        hideLoading()

        if (response.data.status.value === 200) {
            authToken = response.data.data
            currentUser = response.data.meta.user
            setAuthToken(authToken)
            localStorage.setItem("authToken", authToken)
            localStorage.setItem("currentUser", JSON.stringify(currentUser))
            showMessage("Google orqali muvaffaqiyatli kirdingiz!", "success")
            showPage("mainApp")

            connectWebSocket()

            setTimeout(() => {
                loadUserProfile()
                loadAllUsers()
                loadUserConversations()
            }, 1000)
        }
    } catch (error) {
        hideLoading()
        showMessage(error.response?.data?.message || "Xatolik yuz berdi", "error")
    }
})

// USER FUNCTIONS

// Foydalanuvchi profilini yuklash
async function loadUserProfile() {
    if (!currentUser || !authToken) {
        console.log("User yoki token yo'q:", {currentUser, authToken: !!authToken});
        return;
    }

    if (!currentUser.id) {
        console.log("User ID yo'q:", currentUser);
        showMessage("Foydalanuvchi ID topilmadi", "error");
        return;
    }

    try {
        console.log("Profil yuklanmoqda, user ID:", currentUser.id);

        const response = await request.get("/users/getUserById", {
            params: {id: currentUser.id},
        });

        if (response.data.status === "OK") {
            currentUser = response.data.data;
            updateProfileDisplay();
            console.log("Profil muvaffaqiyatli yuklandi:", currentUser);
        } else {
            console.warn("Profil status OK emas:", response.data);
            showMessage("Foydalanuvchi topilmadi", "error");
        }
    } catch (error) {
        console.error("Profil yuklashda xatolik:", error);
        showMessage("Profil yuklashda xatolik", "error");
    }
}


// Profil ma'lumotlarini yangilash
function updateProfileDisplay() {
    if (!currentUser) return

    document.getElementById("profileName").textContent = currentUser.fullname || "Foydalanuvchi"
    document.getElementById("profileEmail").textContent = currentUser.email || ""
    document.getElementById("usernameDisplay").textContent = currentUser.username || ""
    document.getElementById("fullnameDisplay").textContent = currentUser.fullname || ""

    if (currentUser.profilePic) {
        document.getElementById("profileAvatar").src = currentUser.profilePic
    }
}

// Username tahrirlash
function editUsername() {
    const currentUsername = document.getElementById("usernameDisplay").textContent
    const newUsername = prompt("Yangi foydalanuvchi nomini kiriting:", currentUsername)

    if (newUsername && newUsername !== currentUsername) {
        updateUserNameAndFullName(newUsername, currentUser.fullname)
    }
}

// Fullname tahrirlash
function editFullname() {
    const currentFullname = document.getElementById("fullnameDisplay").textContent
    const newFullname = prompt("Yangi to'liq ismingizni kiriting:", currentFullname)

    if (newFullname && newFullname !== currentFullname) {
        updateUserNameAndFullName(currentUser.username, newFullname)
    }
}

// Username va fullname yangilash
async function updateUserNameAndFullName(username, fullname) {
    showLoading()

    try {
        const response = await request.put("/users/updateUserNameAndFullName", null, {
            params: {
                userId: currentUser.id,
                username,
                fullname,
            },
        })

        hideLoading()

        if (response.data.status.value === 200) {
            currentUser.username = username
            currentUser.fullname = fullname
            updateProfileDisplay()
            showMessage("Ma'lumotlar yangilandi", "success")
        }
    } catch (error) {
        hideLoading()
        showMessage(error.response?.data?.message || "Xatolik yuz berdi", "error")
    }
}

// Username tekshirish
async function checkUsername(username) {
    try {
        const response = await request.post("/users/checkUsername", null, {
            params: {username},
        })

        return response.data.status.value === 200
    } catch (error) {
        return false
    }
}

// Barcha faol foydalanuvchilarni yuklash
async function loadAllUsers() {
    if (!authToken) {
        console.log("Token yo'q, foydalanuvchilar yuklanmaydi")
        return
    }

    try {
        console.log("Barcha foydalanuvchilar yuklanmoqda...")

        const response = await request.get("/users/getAllActiveUser", {
            params: {page: 0, size: 50},
        })

        if (response.data.status === "OK") {
            console.log("API Javobi getallActiveUserniki " + response.data)
            allUsers = response.data.data.filter((user) => user.id !== currentUser.id)
            console.log("foydalanuvchilar nechtaligi : " + allUsers.length)
            displayUserCards()
            console.log("Foydalanuvchilar yuklandi:", allUsers.length, "ta user")
        }
    } catch (error) {
        console.error("Foydalanuvchilarni yuklashda xatolik:", error)
        showMessage("Foydalanuvchilarni yuklashda xatolik", "error")
    }
}

// Foydalanuvchi kartalarini ko'rsatish
function displayUserCards() {
    const cardStack = document.getElementById("cardStack")
    cardStack.innerHTML = ""

    if (allUsers.length === 0) {
        cardStack.innerHTML = '<div class="no-users">Hozircha yangi foydalanuvchilar yo\'q</div>'
        return
    }

    allUsers.slice(0, 5).forEach((user, index) => {
        const card = createUserCard(user, index)
        cardStack.appendChild(card)
    })

    currentCard = allUsers[0]
}

// Foydalanuvchi kartasini yaratish
function createUserCard(user, index) {
    const card = document.createElement("div")
    card.className = "user-card"
    card.style.zIndex = 10 - index
    card.dataset.userId = user.id

    card.innerHTML = `
    <img src="${user.profilePic || DEFAULT_AVATAR_400}" alt="${user.fullname}" class="card-image">
    <div class="card-info">
        <div class="card-name">${user.fullname || user.username}</div>
        <div class="card-details">
            <div>@${user.username}</div>
            <div>${user.email}</div>
        </div>
    </div>
`

    return card
}

// SWIPE FUNCTIONS

// Foydalanuvchini swipe qilish
async function swipeUser(status) {
    if (!currentCard) return

    try {
        const response = await request.post("/swipe/swipeUser", null, {
            params: {
                swiperId: currentUser.id,
                swipedId: currentCard.id,
                status: status === "LIKE" ? "ACCEPT" : "REJECT",
            },
        })

        if (response.data.status.value === 200 || response.data.status.value === 202) {
            // Kartani olib tashlash
            const cardElement = document.querySelector(`[data-user-id="${currentCard.id}"]`)
            if (cardElement) {
                cardElement.style.transform = status === "LIKE" ? "translateX(100%)" : "translateX(-100%)"
                cardElement.style.opacity = "0"

                setTimeout(() => {
                    cardElement.remove()
                    nextCard()
                }, 300)
            }

            if (status === "LIKE") {
                // Match bo'ldimi tekshirish (backenddan isMatch kelishi yaxshi)
                if (response.data.isMatch || response.data.message?.includes("Match found")) {
                    showMessage("🎉 Match topildi! Endi suhbat boshlashingiz mumkin!", "success")

                    // Suhbat yaratish yoki olish
                    const conversationResponse = await request.post("/chat/createOrGetConversation", {
                        user1Id: currentUser.id,
                        user2Id: currentCard.id
                    })

                    if (conversationResponse.data.status === "OK") {
                        const conversation = conversationResponse.data.data

                        // Chat oynasini to'g'ridan-to'g'ri ochish
                        showMainPage('chatPage')
                        openChatWindow(
                            conversation.id,
                            currentCard.id,
                            currentCard.fullname || currentCard.username,
                            currentCard.profilePic
                        )
                    }
                } else {
                    showMessage("Like yuborildi! ❤️", "success")
                }
            }
        }
    } catch (error) {
        showMessage(error.response?.data?.message || "Xatolik yuz berdi", "error")
    }
}


function getCurrentConversationId(userId1, userId2) {
    // conversations massividan conversationId ni topish
    const conversation = conversations.find(conv => {
        return (
            (conv.user1.id === userId1 && conv.user2.id === userId2) ||
            (conv.user1.id === userId2 && conv.user2.id === userId1)
        )
    })

    return conversation?.id || null
}

async function loadUserConversations() {
    if (!currentUser || !currentUser.id) return

    try {
        const response = await request.post("/api/notification/send", {
            params: {userId: currentUser.id},
        })

        if (response.data.status === "OK") {
            conversations = response.data.data
            displayConversations(conversations)
        }
    } catch (error) {
        console.error("Suhbatlarni yuklashda xatolik:", error)
    }
}


function displayConversations(conversations) {
    const chatList = document.getElementById("chatList");
    if (!chatList) return;

    if (!conversations || !Array.isArray(conversations)) {
        chatList.innerHTML = '<div class="no-chats">Hozircha suhbatlar yo\'q</div>';
        return;
    }

    chatList.innerHTML = conversations.length === 0
        ? '<div class="no-chats">Hozircha suhbatlar yo\'q</div>'
        : conversations.map(conv => {
            try {
                // Hozirgi foydalanuvchini aniqlash
                const currentUserId = currentUser?.id;

                // Suhbatdosh foydalanuvchini aniqlash
                const isUser1 = conv.user1Id === currentUserId;
                const otherUser = {
                    id: isUser1 ? conv.user2Id : conv.user1Id,
                    fullname: isUser1 ? conv.user2Name : conv.user1Name,
                    // Boshqa maydonlar default qiymatlari
                    username: '',
                    profilePic: DEFAULT_AVATAR_50
                };

                return `
                    <div class="chat-item" onclick="openChatWindow('${conv.id}', '${otherUser.id}', '${otherUser.fullname}', '${otherUser.profilePic}')">
                        <img src="${otherUser.profilePic}" class="chat-avatar">
                        <div class="chat-info">
                            <div class="chat-name">${otherUser.fullname}</div>
                            <div class="chat-last-message">Suhbat boshlang...</div>
                        </div>
                        <div class="chat-time"></div>
                    </div>
                `;
            } catch (e) {
                console.error("Suhbatni ko'rsatishda xatolik:", e, conv);
                return '';
            }
        }).join('');
}

// Keyingi kartaga o'tish
function nextCard() {
    allUsers.shift()
    if (allUsers.length > 0) {
        currentCard = allUsers[0]

        // Agar kartalar kam bo'lsa, yangilarini yuklash
        if (allUsers.length < 3) {
            loadAllUsers()
        }
    } else {
        currentCard = null
        document.getElementById("cardStack").innerHTML =
            '<div class="no-users">Barcha foydalanuvchilarni ko\'rdingiz!</div>'
    }
}

// Mening swipelarimni olish
async function getMySwipes() {
    if (!authToken || !currentUser) {
        showMessage("Avval tizimga kiring", "error")
        return
    }

    showLoading()

    try {
        const response = await request.get("/swipe/getSwipesByUser", {
            params: {userId: currentUser.id},
        })

        hideLoading()

        if (response.data.status === "OK") {
            const swipes = response.data.data
            let content = '<h3>Mening swipelarim</h3><div class="swipes-list">'

            swipes.forEach((swipe) => {
                content += `
                    <div class="swipe-item">
                        <span>${swipe.swipedFullName || "Noma'lum"}</span>
                        <span class="swipe-status ${swipe.status.toLowerCase()}">${swipe.status}</span>
                    </div>
                `
            })

            content += "</div>"
            showModal(content)
        }
    } catch (error) {
        hideLoading()
        showMessage(error.response?.data?.message || "Xatolik yuz berdi", "error")
    }
}

// LOCATION FUNCTIONS

// Joylashuv sahifasini ko'rsatish
function showLocationPage() {
    showMainPage("locationPage")
}

// Joriy joylashuvni olish
function getCurrentLocation() {
    if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(
            (position) => {
                document.getElementById("latitude").value = position.coords.latitude
                document.getElementById("longitude").value = position.coords.longitude
                showMessage("Joylashuv olindi", "success")
            },
            (error) => {
                showMessage("Joylashuvni olishda xatolik", "error")
            },
        )
    } else {
        showMessage("Brauzer joylashuvni qo'llab-quvvatlamaydi", "error")
    }
}

// Joylashuvni yangilash
async function updateLocation() {
    const latitudeInput = document.getElementById("latitude").value.trim()
    const longitudeInput = document.getElementById("longitude").value.trim()

    if (!latitudeInput || !longitudeInput) {
        showMessage("Iltimos, kenglik va uzunlikni kiriting", "error")
        return
    }

    const latitude = parseFloat(latitudeInput)
    const longitude = parseFloat(longitudeInput)

    if (isNaN(latitude) || isNaN(longitude)) {
        showMessage("Noto'g'ri koordinata formati. Iltimos, son kiriting (masalan: 41.123456)", "error")
        return
    }

    showLoading()

    try {

        const response = await request.post("/api/location/update", null, {
            params: {
                userId: currentUser.id,
                latitude: latitude.toString(),
                longitude: longitude.toString()
            }
        })

        hideLoading()

        if (response.data.status === "OK") {
            showMessage("Joylashuv muvaffaqiyatli yangilandi!", "success")
            console.log("Yangilangan joylashuv:", {latitude, longitude})
        } else {
            showMessage(response.data.message || "Joylashuv yangilanmadi", "warning")
        }
    } catch (error) {
        hideLoading()

        const errorMessage = error.response?.data?.message ||
            error.response?.data?.error ||
            "Server bilan aloqada xatolik"

        console.error("Joylashuv yangilash xatosi:", {
            error: errorMessage,
            status: error.response?.status,
            data: error.response?.data
        })

        showMessage(errorMessage, "error")
    }
}

// Yaqin foydalanuvchilarni olish
async function getNearbyUsers() {
    const radiusKm = Number.parseFloat(document.getElementById("radiusKm").value) || 50

    showLoading()

    try {
        const response = await request.get("/api/location/nearby", {
            params: {
                userId: currentUser.id,
                radiusKm,
            },
        })

        hideLoading()

        const nearbyUsers = response.data
        const nearbyList = document.getElementById("nearbyUsersList")

        if (nearbyUsers.length === 0) {
            nearbyList.innerHTML = "<p>Yaqin atrofda foydalanuvchilar topilmadi</p>"
            return
        }

        nearbyList.innerHTML = ""
        nearbyUsers.forEach((user) => {
            const userDiv = document.createElement("div")
            userDiv.className = "nearby-user"
            userDiv.innerHTML = `
    <img src="${user.profilePic || DEFAULT_AVATAR_50}" alt="${user.fullname}">
    <div class="nearby-user-info">
        <div class="nearby-user-name">${user.fullname || user.username}</div>
        <div class="nearby-user-distance">Yaqin atrofda</div>
    </div>
`
            nearbyList.appendChild(userDiv)
        })
    } catch (error) {
        hideLoading()
        console.error("Nearby users error:", error)

        if (error.response?.data?.message?.includes("lokatsiyangiz aniqlanmagan")) {
            showMessage("Avval joylashuvingizni belgilang", "warning")
        } else {
            showMessage(error.response?.data?.message || "Yaqin foydalanuvchilarni yuklashda xatolik", "error")
        }
    }
}

// CHAT FUNCTIONS

// Foydalanuvchi suhbatlarini yuklash
async function loadUserConversations() {
    if (!currentUser?.id) {
        console.log("Foydalanuvchi ID si topilmadi");
        return;
    }

    showLoading();

    try {
        const response = await request.get("/conversation/getUserConversations", {
            params: {userId: currentUser.id}
        });

        console.log("API javobi:", response.data);

        // Ma'lumotlarni qayta ishlash
        let conversations = [];

        if (Array.isArray(response.data)) {
            conversations = response.data;
        } else if (response.data?.data) {
            conversations = Array.isArray(response.data.data) ?
                response.data.data :
                [response.data.data];
        } else if (response.data?.content) {
            conversations = response.data.content;
        }

        displayConversations(conversations);
    } catch (error) {
        console.error("Suhbatlarni yuklashda xatolik:", error);
        showMessage("Suhbatlarni yuklashda xatolik yuz berdi", "error");
        displayConversations([]);
    } finally {
        hideLoading();
    }
}

async function getOrCreateConversation(user1Id, user2Id) {
    showLoading();

    try {
        // Avval conversation borligini tekshiramiz
        const findResponse = await request.get("/conversation/find", {
            params: {user1Id, user2Id}
        });

        if (findResponse.data.status === "OK" && findResponse.data.data) {
            return findResponse.data.data; // Mavjud conversationni qaytaramiz
        }

        // Yangi conversation yaratamiz
        const createResponse = await request.post("/conversation/create", null, {
            params: {user1Id, user2Id}
        });

        if (createResponse.data.status === "OK" || createResponse.data.status?.value === 200) {
            return createResponse.data.data;
        }

        throw new Error("Conversation yaratib bo'lmadi");
    } catch (error) {
        console.error("Conversation olish/yaratishda xatolik:", error);
        showMessage("Suhbat boshlashda xatolik", "error");
        return null;
    } finally {
        hideLoading();
    }
}

async function openChatWithUser(otherUserId) {
    const conversation = await getOrCreateConversation(currentUser.id, otherUserId);
    if (!conversation) return;

    // Suhbatdosh foydalanuvchini aniqlash
    const otherUser = conversation.user1.id === currentUser.id ? conversation.user2 : conversation.user1;

    // UI ni sozlash
    currentConversationId = conversation.id;
    currentChatUser = {
        id: otherUser.id,
        name: otherUser.fullname || otherUser.username,
        avatar: otherUser.profilePic
    };

    document.getElementById("chatUserName").textContent = currentChatUser.name;
    document.getElementById("chatUserAvatar").src = currentChatUser.avatar || DEFAULT_AVATAR_40;
    document.getElementById("chatWindow").classList.remove("hidden");
    document.getElementById("messagesContainer").innerHTML = "";

    // Xabarlarni yuklash
    loadChatMessages(conversation.id);
}

// Suhbatlarni ko'rsatish
function displayConversations(conversations) {
    const chatList = document.getElementById("chatList");
    if (!chatList) {
        console.error("chatList element topilmadi");
        return;
    }

    // Ma'lumotlarni tekshirish
    if (!conversations || !Array.isArray(conversations)) {
        console.warn("Noto'g'ri suhbatlar formati:", conversations);
        chatList.innerHTML = '<div class="no-chats">Hozircha suhbatlar yo\'q</div>';
        return;
    }

    // Agar suhbatlar bo'sh bo'lsa
    if (conversations.length === 0) {
        chatList.innerHTML = '<div class="no-chats">Hozircha suhbatlar yo\'q</div>';
        return;
    }

    // Suhbatlarni ko'rsatish
    chatList.innerHTML = conversations.map(conv => {
        try {
            // Hozirgi foydalanuvchi ID sini tekshirish
            if (!currentUser || !currentUser.id) {
                console.error("Current user topilmadi");
                return '';
            }

            // Suhbat ma'lumotlarini tekshirish
            if (!conv || !conv.id || !conv.user1Id || !conv.user2Id) {
                console.warn("Noto'g'ri suhbat formati:", conv);
                return '';
            }

            // Suhbatdosh foydalanuvchini aniqlash
            const isUser1 = conv.user1Id === currentUser.id;
            const otherUserId = isUser1 ? conv.user2Id : conv.user1Id;
            const otherUserName = isUser1 ? conv.user2Name : conv.user1Name;

            // Suhbatdosh foydalanuvchi obyekti
            const otherUser = {
                id: otherUserId,
                fullname: otherUserName,
                username: '',
                profilePic: DEFAULT_AVATAR_50
            };

            // Xatolikni oldini olish uchun HTML kodini yaratish
            const safeId = conv.id ? conv.id.toString() : '';
            const safeUserId = otherUser.id ? otherUser.id.toString() : '';
            const safeName = otherUser.fullname ?
                otherUser.fullname.replace(/"/g, '&quot;') :
                'Foydalanuvchi';
            const safeAvatar = otherUser.profilePic || DEFAULT_AVATAR_50;

            // Oxirgi xabarni aniqlash
            let lastMessage = 'Suhbat boshlang...';
            if (conv.messages && conv.messages.length > 0) {
                const lastMsg = conv.messages[conv.messages.length - 1];
                lastMessage = lastMsg.message.substring(0, 30);
                if (lastMsg.message.length > 30) lastMessage += '...';
            }

            return `
                <div class="chat-item" 
                     onclick="openChatWindow('${safeId}', '${safeUserId}', '${safeName}', '${safeAvatar}')">
                    <img src="${safeAvatar}" class="chat-avatar" 
                         alt="${safeName}">
                    <div class="chat-info">
                        <div class="chat-name">${safeName}</div>
                        <div class="chat-last-message">${lastMessage}</div>
                    </div>
                    <div class="chat-time">${formatLastMessageTime(conv)}</div>
                </div>
            `;
        } catch (e) {
            console.error("Suhbatni ko'rsatishda xatolik:", e, conv);
            return '';
        }
    }).join('');
}

function formatLastMessageTime(conversation) {
    if (!conversation.messages || conversation.messages.length === 0) return "";

    const lastMsg = conversation.messages[conversation.messages.length - 1];
    const msgTime = lastMsg.timestamp || lastMsg.createdAt;

    if (!msgTime) return "";

    try {
        const date = new Date(msgTime);
        return date.toLocaleTimeString([], {hour: '2-digit', minute: '2-digit'});
    } catch {
        return "";
    }
}

// Chat oynasini ochish
function openChatWindow(conversationId, userId, userName, userAvatar) {
    currentConversationId = conversationId;
    currentChatUser = {
        id: userId,
        name: userName,
        avatar: userAvatar || DEFAULT_AVATAR_40
    };

    // UI ni yangilash
    document.getElementById("chatUserName").textContent = userName;
    document.getElementById("chatUserAvatar").src = currentChatUser.avatar;
    document.getElementById("chatWindow").classList.remove("hidden");
    document.getElementById("messagesContainer").innerHTML = "";

    // Xabarlarni yuklash
    loadChatMessages(conversationId);

    // WebSocket obunasini yangilash
    if (stompClient && stompClient.connected) {
        subscribeToChat(conversationId);
    } else {
        console.log("WebSocket ulanmagan, qayta ulanmoqda...");
        connectWebSocket();
    }
}

// Chat xabarlarini yuklash funksiyasi
async function loadChatMessages(conversationId) {
    try {
        showLoading();

        const response = await request.get("/chat/messages", {
            params: {conversationId}
        });

        if (response.data && Array.isArray(response.data)) {
            const messagesContainer = document.getElementById("messagesContainer");
            messagesContainer.innerHTML = "";

            response.data.forEach(message => {
                // Har bir xabarni qaysi tomondan ekanligini aniqlash
                const isSent = message.senderId === currentUser.id;
                addMessageToChat(message, isSent);
            });

            // Xabarlar ro'yxatini pastga scroll qilish
            messagesContainer.scrollTop = messagesContainer.scrollHeight;
        } else {
            console.error("Noto'g'ri javob formati:", response.data);
            showMessage("Xabarlarni yuklashda xatolik", "error");
        }
    } catch (error) {
        console.error("Xabarlarni yuklashda xatolik:", error);
        showMessage(error.response?.data?.message || "Xabarlarni yuklashda xatolik", "error");
    } finally {
        hideLoading();
    }
}

// Chat oynasini yopish
function closeChatWindow() {
    document.getElementById("chatWindow").classList.add("hidden")
    currentChatUser = null
    currentConversationId = null
}

// Xabar yuborish
function sendMessage() {
    const messageInput = document.getElementById("messageInput");
    const messageText = messageInput.value.trim();

    if (!messageText || !currentChatUser || !currentConversationId) return;

    // messageInput qiymatini tozalash
    messageInput.value = "";

    // WebSocket orqali yuborish
    if (stompClient && stompClient.connected) {
        stompClient.send("/app/chat/send", {}, JSON.stringify({
            senderId: currentUser.id,
            receiverId: currentChatUser.id,
            message: messageText,
            conversationId: currentConversationId
        }));
    } else {
        console.error("WebSocket ulanmagan");
        showMessage("Xabar yuborishda xatolik. Qayta ulanmoqda...", "error");
        connectWebSocket();
    }
}


// Xabarni chat ga qo'shish
function addMessageToChat(messageData, isSent) {
    const messagesContainer = document.getElementById("messagesContainer");
    if (!messagesContainer) return;

    const messageDiv = document.createElement("div");
    messageDiv.className = `message ${isSent ? "sent" : "received"}`;

    // Matnni tozalash
    const cleanMessage = (messageData.message || '').trim().replace(/\s+/g, ' ');

    // Vaqtni formatlash
    const messageTime = new Date(messageData.timestamp || messageData.createdAt || new Date()).toLocaleTimeString([], {
        hour: '2-digit',
        minute: '2-digit'
    });

    // Foydalanuvchi nomi
    const senderName = messageData.senderName ||
        (isSent ? (currentUser.fullname || currentUser.username) :
            (currentChatUser?.name || 'Foydalanuvchi'));

    messageDiv.innerHTML = `
        ${!isSent ? `<img src="${currentChatUser?.avatar || DEFAULT_AVATAR_40}" class="message-avatar" alt="${senderName}">` : ''}
        <div class="message-content">
            ${!isSent ? `<div class="message-sender">${senderName}</div>` : ''}
            <div class="message-bubble">
                <div class="message-text">${cleanMessage}</div>
                <div class="message-time">${messageTime}</div>
            </div>
        </div>
        ${isSent ? `<img src="${currentUser.profilePic || DEFAULT_AVATAR_40}" class="message-avatar" alt="You">` : ''}
    `;

    messagesContainer.appendChild(messageDiv);
    messagesContainer.scrollTop = messagesContainer.scrollHeight;
}


// Enter tugmasi bilan xabar yuborish
document.getElementById("messageInput").addEventListener("keypress", (e) => {
    if (e.key === "Enter") {
        sendMessage()
    }
})

// NOTIFICATION FUNCTIONS

// Test bildirishnoma yuborish
async function sendTestNotification() {
    showLoading()

    try {
        const response = await request.post("/api/notification/send", {
            toUserId: currentUser.id,
            title: "Test bildirishnoma",
            message: "Bu test bildirishnomasidir",
        })

        hideLoading()

        if (response.data.status.value === 200) {
            showMessage("Bildirishnoma yuborildi", "success")
        }
    } catch (error) {
        hideLoading()
        showMessage(error.response?.data?.message || "Xatolik yuz berdi", "error")
    }
}

// SETTINGS FUNCTIONS

// Email o'zgartirish
function changeEmail() {
    const newEmail = prompt("Yangi email manzilni kiriting:", currentUser.email)

    if (newEmail && newEmail !== currentUser.email) {
        updateEmail(currentUser.email, newEmail)
    }
}

// Email yangilash
async function updateEmail(oldEmail, newEmail) {
    showLoading()

    try {
        const response = await request.put("/users/updateEmail", null, {
            params: {email: oldEmail, newEmail},
        })

        hideLoading()

        if (response.data.status === "OK") {
            showMessage("Tasdiqlash kodi yuborildi", "info")

            const code = prompt("Email manzilingizga yuborilgan kodni kiriting:")
            if (code) {
                checkUpdatedEmail(oldEmail, newEmail, code)
            }
        }
    } catch (error) {
        hideLoading()
        showMessage(error.response?.data?.message || "Xatolik yuz berdi", "error")
    }
}

// Email yangilanishini tasdiqlash
async function checkUpdatedEmail(oldEmail, newEmail, code) {
    showLoading()

    try {
        const response = await request.post("/users/checkUpdatedEmail", null, {
            params: {oldEmail, newEmail, code},
        })

        hideLoading()

        if (response.data.status === "OK") {
            currentUser.email = newEmail
            updateProfileDisplay()
            showMessage("Email muvaffaqiyatli yangilandi", "success")
        }
    } catch (error) {
        hideLoading()
        showMessage(error.response?.data?.message || "Xatolik yuz berdi", "error")
    }
}

// Parol o'zgartirish
function changePassword() {
    const newPassword = prompt("Yangi parolni kiriting:")

    if (newPassword && newPassword.length >= 6) {
        updatePassword(currentUser.email, newPassword)
    } else {
        showMessage("Parol kamida 6 ta belgidan iborat bo'lishi kerak", "error")
    }
}

// Parol yangilash
async function updatePassword(email, newPassword) {
    showLoading()

    try {
        const response = await request.put("/users/updatePassword", null, {
            params: {email, newPassword},
        })

        hideLoading()

        if (response.data.status.value === 200) {
            showMessage("Tasdiqlash kodi yuborildi", "info")

            const code = prompt("Email manzilingizga yuborilgan kodni kiriting:")
            if (code) {
                checkUpdatedPassword(email, newPassword, code)
            }
        }
    } catch (error) {
        hideLoading()
        showMessage(error.response?.data?.message || "Xatolik yuz berdi", "error")
    }
}

// Parol yangilanishini tasdiqlash
async function checkUpdatedPassword(email, password, code) {
    showLoading()

    try {
        const response = await request.post("/users/checkUpdatedPassword", null, {
            params: {email, password, code},
        })

        hideLoading()

        if (response.data.status.value === 200) {
            showMessage("Parol muvaffaqiyatli yangilandi", "success")
        }
    } catch (error) {
        hideLoading()
        showMessage(error.response?.data?.message || "Xatolik yuz berdi", "error")
    }
}

// Profil rasmini o'zgartirish
async function changeProfilePhoto() {
    const fileInput = document.createElement("input")
    fileInput.type = "file"
    fileInput.accept = "image/*"

    fileInput.onchange = async (e) => {
        const file = e.target.files[0]
        if (!file) return

        try {
            const formData = new FormData()
            formData.append("file", file)

            const uploadRes = await axios.post("/api/attachments/upload", formData, {
                headers: {
                    Authorization: `Bearer ${authToken}`
                }
            })

            const attachmentId = uploadRes.data?.data?.id
            if (!attachmentId) throw new Error("attachmentId topilmadi")

            await axios.put("/users/updateProfilePhoto", null, {
                params: {
                    attachmentId,
                    userId: currentUser.id
                },
                headers: {
                    Authorization: `Bearer ${authToken}`
                }
            })

            // Rasmni frontendda yangilash
            document.getElementById("profileAvatar").src = `/api/attachments/view/${attachmentId}`
            showMessage("Profil rasmi muvaffaqiyatli yangilandi", "success")
        } catch (err) {
            console.error("Rasmni yangilashda xatolik:", err)
            showMessage("Xatolik yuz berdi", "error")
        }
    }

    fileInput.click()
}


// Chiqish
function logout() {
    if (confirm("Haqiqatan ham chiqmoqchimisiz?")) {
        disconnectWebSocket()
        localStorage.removeItem("authToken")
        localStorage.removeItem("currentUser")
        localStorage.removeItem("token")
        setAuthToken(null)
        currentUser = null
        showPage("loginPage")
        showMessage("Muvaffaqiyatli chiqdingiz", "info")
    }
}

// INITIALIZATION

// Sahifa yuklanganda
document.addEventListener("DOMContentLoaded", () => {
    console.log("Sahifa yuklandi")

    // SockJS va Stomp.js kutubxonalarini yuklash
    if (typeof window.SockJS === "undefined" || typeof window.Stomp === "undefined") {
        const sockjsScript = document.createElement("script")
        sockjsScript.src = "https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js"
        document.head.appendChild(sockjsScript)

        const stompScript = document.createElement("script")
        stompScript.src = "https://cdn.jsdelivr.net/npm/@stomp/stompjs@6.1.2/bundles/stomp.umd.min.js"
        document.head.appendChild(stompScript)
    }

    // Saqlangan tokenni tekshirish
    const savedToken = localStorage.getItem("authToken") || localStorage.getItem("token")
    const savedUser = localStorage.getItem("currentUser")

    console.log("Saqlangan ma'lumotlar:", {
        hasToken: !!savedToken,
        hasUser: !!savedUser,
    })

    if (savedToken && savedUser) {
        try {
            authToken = savedToken
            currentUser = JSON.parse(savedUser)
            setAuthToken(authToken)

            console.log("Token va user tiklandi")

            // Tokenni tekshirish
            checkTokenValidity().then((isValid) => {
                if (isValid) {
                    console.log("Token yaroqli, main app ko'rsatilmoqda")
                    showPage("mainApp")
                    connectWebSocket()
                    setTimeout(() => {
                        loadUserProfile()
                        loadAllUsers()
                        loadUserConversations()
                    }, 1000)
                } else {
                    console.log("Token yaroqsiz, logout qilinyapti")
                    logout()
                }
            })
        } catch (error) {
            console.error("Ma'lumotlarni tiklashda xatolik:", error)
            logout()
        }
    } else {
        console.log("Saqlangan ma'lumotlar yo'q, login sahifasi ko'rsatilmoqda")
        showPage("loginPage")
    }

    // Device ID ni avtomatik generatsiya qilish
    if (document.getElementById("loginDeviceId") && !document.getElementById("loginDeviceId").value) {
        document.getElementById("loginDeviceId").value = generateDeviceId()
    }
    if (document.getElementById("regDeviceId") && !document.getElementById("regDeviceId").value) {
        document.getElementById("regDeviceId").value = generateDeviceId()
    }
})

// Token yaroqliligini tekshirish
async function checkTokenValidity() {
    if (!authToken) {
        console.log("Token yo'q")
        return false
    }

    try {
        console.log("Token tekshirilmoqda...")

        const response = await request.get("/users/getUserByToken", {
            params: {token: authToken},
        })

        console.log("Token tekshiruv javobi:", response.data)
        return response.data.status.value === 200
    } catch (error) {
        console.error("Token tekshirishda xatolik:", error)
        return false
    }
}

// Chat sahifasi ochilganda suhbatlarni yuklash
const originalShowMainPage = showMainPage
showMainPage = function (pageId) {
    originalShowMainPage.call(this, pageId)

    if (pageId === "chatPage") {
        loadUserConversations()
    }
}

// Swipe gesture support (touch events)
let startX = 0
let startY = 0
let currentX = 0
let currentY = 0
let isDragging = false

document.addEventListener("touchstart", (e) => {
    if (e.target.closest(".user-card")) {
        startX = e.touches[0].clientX
        startY = e.touches[0].clientY
        isDragging = true
    }
})

document.addEventListener("touchmove", (e) => {
    if (!isDragging) return

    currentX = e.touches[0].clientX
    currentY = e.touches[0].clientY

    const deltaX = currentX - startX
    const card = e.target.closest(".user-card")

    if (card) {
        card.style.transform = `translateX(${deltaX}px) rotate(${deltaX * 0.1}deg)`
        card.style.opacity = 1 - Math.abs(deltaX) / 300
    }
})

document.addEventListener("touchend", (e) => {
    if (!isDragging) return

    const deltaX = currentX - startX
    const card = e.target.closest(".user-card")

    if (card) {
        if (Math.abs(deltaX) > 100) {
            // Swipe qilindi
            const status = deltaX > 0 ? "LIKE" : "DISLIKE"
            swipeUser(status)
        } else {
            // Qaytarish
            card.style.transform = ""
            card.style.opacity = ""
        }
    }

    isDragging = false
})

// Keyboard shortcuts
document.addEventListener("keydown", (e) => {
    if (document.getElementById("swipePage") && document.getElementById("swipePage").classList.contains("active")) {
        if (e.key === "ArrowLeft") {
            swipeUser("DISLIKE")
        } else if (e.key === "ArrowRight") {
            swipeUser("LIKE")
        }
    }
})

// Network status monitoring
window.addEventListener("online", () => {
    showMessage("Internet aloqasi tiklandi", "success")
    if (currentUser && currentUser.id && !window.stompClient) {
        connectWebSocket()
    }
})

window.addEventListener("offline", () => {
    showMessage("Internet aloqasi uzildi", "error")
    disconnectWebSocket()
})

console.log("Tinder Frontend yuklandi! 🔥")
