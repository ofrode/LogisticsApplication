const navToggle = document.querySelector(".nav-toggle");
const navMenu = document.querySelector(".nav-menu");
const requestForm = document.querySelector("#request-form");
const formStatus = document.querySelector(".request-form-status");
const accountModal = document.querySelector("#account-modal");
const accountOpenButtons = document.querySelectorAll("[data-account-modal-open]");
const accountCloseButtons = document.querySelectorAll("[data-account-modal-close]");
const accountTabButtons = document.querySelectorAll("[data-account-tab]");
const createUserForm = document.querySelector("#create-user-form");
const createUserFormStatus = document.querySelector(".create-user-form-status");
const registerForm = document.querySelector("#register-form");
const registerFormStatus = document.querySelector(".register-form-status");

if (navToggle && navMenu) {
  navToggle.addEventListener("click", () => {
    const isOpen = navMenu.classList.toggle("is-open");
    navToggle.setAttribute("aria-expanded", String(isOpen));
  });

  navMenu.addEventListener("click", (event) => {
    if (event.target instanceof HTMLAnchorElement || event.target instanceof HTMLButtonElement) {
      navMenu.classList.remove("is-open");
      navToggle.setAttribute("aria-expanded", "false");
    }
  });
}

if (requestForm && formStatus) {
  requestForm.addEventListener("submit", (event) => {
    event.preventDefault();
    formStatus.textContent = "Заявка подготовлена. Подключите endpoint для реальной отправки.";
    requestForm.reset();
  });
}

function openAccountModal() {
  if (!accountModal) {
    return;
  }

  accountModal.classList.add("is-open");
  accountModal.setAttribute("aria-hidden", "false");
  document.body.classList.add("modal-open");
}

function closeAccountModal() {
  if (!accountModal) {
    return;
  }

  accountModal.classList.remove("is-open");
  accountModal.setAttribute("aria-hidden", "true");
  document.body.classList.remove("modal-open");
}

function activateAccountTab(tabName) {
  accountTabButtons.forEach((button) => {
    const isActive = button.dataset.accountTab === tabName;
    button.classList.toggle("is-active", isActive);
    button.setAttribute("aria-selected", String(isActive));
  });

  document.querySelectorAll(".account-panel").forEach((panel) => {
    const isActive = panel.id === `${tabName}-user-panel` || panel.id === `${tabName}-panel`;
    panel.classList.toggle("is-active", isActive);
    panel.toggleAttribute("hidden", !isActive);
  });
}

async function submitUserForm(form, statusElement, successMessage) {
  if (!form || !statusElement) {
    return;
  }

  statusElement.textContent = "Отправляем данные...";

  const formData = new FormData(form);
  const payload = Object.fromEntries(formData.entries());
  payload.email = String(payload.email).trim().toLowerCase();

  try {
    const response = await fetch("/api/users", {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify(payload)
    });

    if (!response.ok) {
      const error = await response.json().catch(() => ({}));
      throw new Error(error.message || `Ошибка ${response.status}`);
    }

    const user = await response.json();
    statusElement.textContent = `${successMessage} Логин: ${user.login || user.email}`;
    form.reset();
  } catch (error) {
    statusElement.textContent = error.message || "Не удалось выполнить действие.";
  }
}

accountOpenButtons.forEach((button) => {
  button.addEventListener("click", openAccountModal);
});

accountCloseButtons.forEach((button) => {
  button.addEventListener("click", closeAccountModal);
});

accountTabButtons.forEach((button) => {
  button.addEventListener("click", () => activateAccountTab(button.dataset.accountTab));
});

document.addEventListener("keydown", (event) => {
  if (event.key === "Escape") {
    closeAccountModal();
  }
});

if (createUserForm && createUserFormStatus) {
  createUserForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    await submitUserForm(createUserForm, createUserFormStatus, "Пользователь создан.");
  });
}

if (registerForm && registerFormStatus) {
  registerForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    await submitUserForm(registerForm, registerFormStatus, "Регистрация завершена.");
  });
}
