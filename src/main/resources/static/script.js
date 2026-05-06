const navToggle = document.querySelector(".nav-toggle");
const navMenu = document.querySelector(".nav-menu");
const pageType = document.body.dataset.page;
const loginForm = document.querySelector("#login-form");
const loginFormStatus = document.querySelector(".login-form-status");
const registerForm = document.querySelector("#register-form");
const registerFormStatus = document.querySelector(".register-form-status");

const adminUsersList = document.querySelector("#admin-users-list");
const adminUsersStatus = document.querySelector(".admin-users-status");
const adminUserForm = document.querySelector("#admin-user-form");
const adminUserFormStatus = document.querySelector(".admin-user-form-status");
const adminUserFormReset = document.querySelector("#admin-user-form-reset");
const refreshUsersButton = document.querySelector("#refresh-users-button");

const adminShipmentsList = document.querySelector("#admin-shipments-list");
const adminShipmentsStatus = document.querySelector(".admin-shipments-status");
const adminShipmentForm = document.querySelector("#admin-shipment-form");
const adminShipmentFormStatus = document.querySelector(".admin-shipment-form-status");
const adminShipmentFormReset = document.querySelector("#admin-shipment-form-reset");
const refreshShipmentsButton = document.querySelector("#refresh-shipments-button");

const adminVehiclesList = document.querySelector("#admin-vehicles-list");
const adminVehiclesStatus = document.querySelector(".admin-vehicles-status");
const refreshVehiclesButton = document.querySelector("#refresh-vehicles-button");

const adminCargoesList = document.querySelector("#admin-cargoes-list");
const adminCargoesStatus = document.querySelector(".admin-cargoes-status");
const adminCargoForm = document.querySelector("#admin-cargo-form");
const adminCargoFormStatus = document.querySelector(".admin-cargo-form-status");
const adminCargoFormReset = document.querySelector("#admin-cargo-form-reset");
const refreshCargoesButton = document.querySelector("#refresh-cargoes-button");

const managerShipmentsList = document.querySelector("#manager-shipments-list");
const managerShipmentsStatus = document.querySelector(".manager-shipments-status");
const managerShipmentForm = document.querySelector("#manager-shipment-form");
const managerShipmentFormStatus = document.querySelector(".manager-shipment-form-status");
const managerShipmentFormReset = document.querySelector("#manager-shipment-form-reset");
const managerShipmentCreate = document.querySelector("#manager-shipment-create");
const refreshManagerShipmentsButton = document.querySelector("#refresh-manager-shipments-button");

const managerVehiclesList = document.querySelector("#manager-vehicles-list");
const managerVehiclesStatus = document.querySelector(".manager-vehicles-status");
const refreshManagerVehiclesButton = document.querySelector("#refresh-manager-vehicles-button");

const managerCargoesList = document.querySelector("#manager-cargoes-list");
const managerCargoesStatus = document.querySelector(".manager-cargoes-status");
const refreshManagerCargoesButton = document.querySelector("#refresh-manager-cargoes-button");

const customerShipmentsList = document.querySelector("#customer-shipments-list");
const customerShipmentsStatus = document.querySelector(".customer-shipments-status");
const refreshCustomerShipmentsButton = document.querySelector("#refresh-customer-shipments-button");

const carrierShipmentsList = document.querySelector("#carrier-shipments-list");
const carrierShipmentsStatus = document.querySelector(".carrier-shipments-status");
const refreshCarrierShipmentsButton = document.querySelector("#refresh-carrier-shipments-button");

const carrierVehiclesList = document.querySelector("#carrier-vehicles-list");
const carrierVehiclesStatus = document.querySelector(".carrier-vehicles-status");
const carrierVehicleForm = document.querySelector("#carrier-vehicle-form");
const carrierVehicleFormStatus = document.querySelector(".carrier-vehicle-form-status");
const carrierVehicleFormReset = document.querySelector("#carrier-vehicle-form-reset");
const carrierVehicleCreate = document.querySelector("#carrier-vehicle-create");
const refreshCarrierVehiclesButton = document.querySelector("#refresh-carrier-vehicles-button");

const carrierCargoesList = document.querySelector("#carrier-cargoes-list");
const carrierCargoesStatus = document.querySelector(".carrier-cargoes-status");
const refreshCarrierCargoesButton = document.querySelector("#refresh-carrier-cargoes-button");

let cachedShipments = [];

function saveCurrentUser(user) {
  window.localStorage.setItem("logistics-current-user", JSON.stringify(user));
}

function getCurrentUser() {
  const rawValue = window.localStorage.getItem("logistics-current-user");
  if (!rawValue) {
    return null;
  }
  try {
    return JSON.parse(rawValue);
  } catch {
    return null;
  }
}

if (navToggle && navMenu) {
  navToggle.addEventListener("click", () => {
    const isOpen = navMenu.classList.toggle("is-open");
    navToggle.setAttribute("aria-expanded", String(isOpen));
  });

  navMenu.addEventListener("click", (event) => {
    if (event.target instanceof HTMLAnchorElement) {
      navMenu.classList.remove("is-open");
      navToggle.setAttribute("aria-expanded", "false");
    }
  });
}

function escapeHtml(value) {
  return String(value)
      .replaceAll("&", "&amp;")
      .replaceAll("<", "&lt;")
      .replaceAll(">", "&gt;")
      .replaceAll("\"", "&quot;")
      .replaceAll("'", "&#39;");
}

function formatDateTimeForInput(value) {
  if (!value) {
    return "";
  }
  return String(value).slice(0, 16);
}

function parseCsvIds(value) {
  return String(value)
      .split(",")
      .map((item) => item.trim())
      .filter(Boolean)
      .map((item) => Number(item));
}

function getSelectedNumericValues(selectElement) {
  return Array.from(selectElement.selectedOptions).map((option) => Number(option.value));
}

function renderUserSelectOptions(selectElement, users, role) {
  if (!selectElement) {
    return;
  }
  const filteredUsers = users.filter((user) => user.role === role);
  selectElement.innerHTML = filteredUsers.map((user) =>
    `<option value="${user.id}">${escapeHtml(`${user.firstName} ${user.lastName} (${user.login})`)}</option>`
  ).join("");
}

function renderVehicleSelectOptions(selectElement, vehicles) {
  if (!selectElement) {
    return;
  }
  selectElement.innerHTML = vehicles.map((vehicle) =>
    `<option value="${vehicle.id}">${escapeHtml(`${vehicle.registrationNumber} (${vehicle.capacityKg} кг)`)}</option>`
  ).join("");
}

async function hydrateShipmentFormOptions(form, statusElement) {
  if (!form) {
    return;
  }
  try {
    const [usersResponse, vehiclesResponse] = await Promise.all([
      fetch("/api/users"),
      fetch("/api/vehicles")
    ]);
    if (!usersResponse.ok || !vehiclesResponse.ok) {
      throw new Error("Не удалось загрузить списки клиентов, менеджеров и транспорта.");
    }
    const users = await usersResponse.json();
    const vehicles = await vehiclesResponse.json();
    renderUserSelectOptions(form.elements.customerId, users, "CUSTOMER");
    renderUserSelectOptions(form.elements.managerId, users, "MANAGER");
    renderVehicleSelectOptions(form.elements.vehicleIds, vehicles);
  } catch (error) {
    resetStatus(statusElement, error.message || "Не удалось загрузить списки выбора.");
  }
}

function renderEmpty(container, message) {
  container.innerHTML = `<p class="empty-state">${escapeHtml(message)}</p>`;
}

function resetStatus(element, message) {
  if (element) {
    element.textContent = message;
  }
}

if (registerForm && registerFormStatus) {
  registerForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    registerFormStatus.textContent = "Отправляем данные...";

    const formData = new FormData(registerForm);
    const payload = Object.fromEntries(formData.entries());
    payload.email = String(payload.email).trim().toLowerCase();

    try {
      const response = await fetch("/api/auth/register", {
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
      registerFormStatus.textContent = `Пользователь создан: ${user.login || user.email}`;
      registerForm.reset();
    } catch (error) {
      registerFormStatus.textContent = error.message || "Не удалось создать пользователя.";
    }
  });
}

if (loginForm && loginFormStatus) {
  loginForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    loginFormStatus.textContent = "Проверяем данные...";

    const formData = new FormData(loginForm);
    const payload = Object.fromEntries(formData.entries());

    try {
      const response = await fetch("/api/auth/login", {
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

      const result = await response.json();
      saveCurrentUser(result.user);
      loginFormStatus.textContent = `Вход выполнен. Переходим в кабинет ${result.role}.`;
      window.location.href = result.redirectUrl;
    } catch (error) {
      loginFormStatus.textContent = error.message || "Не удалось выполнить вход.";
    }
  });
}

function fillAdminUserForm(user) {
  adminUserForm.elements.id.value = String(user.id);
  adminUserForm.elements.firstName.value = user.firstName;
  adminUserForm.elements.lastName.value = user.lastName;
  adminUserForm.elements.email.value = user.email;
  adminUserForm.elements.login.value = user.login;
  adminUserForm.elements.role.value = user.role;
  adminUserForm.elements.password.value = "";
  resetStatus(adminUserFormStatus, `Редактируется пользователь #${user.id}.`);
}

function resetAdminUserForm() {
  if (!adminUserForm) {
    return;
  }
  adminUserForm.reset();
  adminUserForm.elements.id.value = "";
  resetStatus(adminUserFormStatus, "Выберите пользователя из списка для редактирования.");
}

function updateUserStats(users) {
  const admins = users.filter((user) => user.role === "ADMIN").length;
  const managers = users.filter((user) => user.role === "MANAGER").length;
  const others = users.length - admins - managers;
  document.querySelector("[data-user-stat='total']").textContent = String(users.length);
  document.querySelector("[data-user-stat='admins']").textContent = String(admins);
  document.querySelector("[data-user-stat='managers']").textContent = String(managers);
  document.querySelector("[data-user-stat='others']").textContent = String(others);
}

function renderUsers(users) {
  if (!users.length) {
    renderEmpty(adminUsersList, "Пользователи пока не найдены.");
    updateUserStats(users);
    return;
  }

  adminUsersList.innerHTML = users.map((user) => `
    <article class="user-row">
      <div class="user-row__main">
        <strong>${escapeHtml(`${user.firstName} ${user.lastName}`)}</strong>
        <span>${escapeHtml(user.email)}</span>
      </div>
      <div class="user-row__meta">
        <span class="user-badge">${escapeHtml(user.role)}</span>
        <span>${escapeHtml(user.login)}</span>
      </div>
      <div class="user-row__actions">
        <button class="button button--secondary" type="button" data-user-edit='${escapeHtml(JSON.stringify(user))}'>Изменить</button>
        <button class="button button--danger" type="button" data-user-delete="${user.id}">Удалить</button>
      </div>
    </article>
  `).join("");

  updateUserStats(users);
}

async function loadUsers() {
  resetStatus(adminUsersStatus, "Загружаем пользователей...");
  try {
    const response = await fetch("/api/users");
    if (!response.ok) {
      const error = await response.json().catch(() => ({}));
      throw new Error(error.message || `Ошибка ${response.status}`);
    }
    const users = await response.json();
    renderUsers(users);
    resetStatus(adminUsersStatus, `Загружено пользователей: ${users.length}.`);
  } catch (error) {
    resetStatus(adminUsersStatus, error.message || "Не удалось загрузить пользователей.");
  }
}

async function deleteUser(userId) {
  resetStatus(adminUsersStatus, "Удаляем пользователя...");
  try {
    const response = await fetch(`/api/users/${userId}`, { method: "DELETE" });
    if (!response.ok) {
      const error = await response.json().catch(() => ({}));
      throw new Error(error.message || `Ошибка ${response.status}`);
    }
    if (adminUserForm.elements.id.value === String(userId)) {
      resetAdminUserForm();
    }
    await loadUsers();
    resetStatus(adminUsersStatus, `Пользователь #${userId} удалён.`);
  } catch (error) {
    resetStatus(adminUsersStatus, error.message || "Не удалось удалить пользователя.");
  }
}

function fillShipmentForm(shipment) {
  adminShipmentForm.elements.id.value = String(shipment.id);
  adminShipmentForm.elements.trackingNumber.value = shipment.trackingNumber;
  adminShipmentForm.elements.originCity.value = shipment.originCity;
  adminShipmentForm.elements.destinationCity.value = shipment.destinationCity;
  adminShipmentForm.elements.status.value = shipment.status;
  adminShipmentForm.elements.customerId.value = String(shipment.customer.id);
  adminShipmentForm.elements.managerId.value = String(shipment.manager.id);
  const selectedAdminVehicleIds = new Set(shipment.vehicles.map((vehicle) => String(vehicle.id)));
  Array.from(adminShipmentForm.elements.vehicleIds.options).forEach((option) => {
    option.selected = selectedAdminVehicleIds.has(option.value);
  });
  adminShipmentForm.elements.orderCreatedAt.value = formatDateTimeForInput(shipment.schedule.orderCreatedAt);
  adminShipmentForm.elements.orderReceivedAt.value = formatDateTimeForInput(shipment.schedule.orderReceivedAt);
  adminShipmentForm.elements.arrivalAt.value = formatDateTimeForInput(shipment.schedule.arrivalAt);
  resetStatus(adminShipmentFormStatus, `Редактируется заявка #${shipment.id}.`);
}

function resetShipmentForm() {
  adminShipmentForm.reset();
  adminShipmentForm.elements.id.value = "";
  resetStatus(adminShipmentFormStatus, "Выберите заявку из списка для редактирования.");
}

function renderShipments(shipments) {
  if (!shipments.length) {
    renderEmpty(adminShipmentsList, "Заявки пока не найдены.");
    return;
  }

  adminShipmentsList.innerHTML = shipments.map((shipment) => `
    <article class="user-row">
      <div class="user-row__main">
        <strong>${escapeHtml(shipment.trackingNumber)}</strong>
        <span>${escapeHtml(`${shipment.originCity} -> ${shipment.destinationCity}`)}</span>
      </div>
      <div class="user-row__meta">
        <span class="user-badge">${escapeHtml(shipment.status)}</span>
        <span>Грузы: ${shipment.cargoes.length}, Транспорт: ${shipment.vehicles.length}</span>
      </div>
      <div class="user-row__actions">
        <button class="button button--secondary" type="button" data-shipment-edit="${shipment.id}">Изменить</button>
        <button class="button button--danger" type="button" data-shipment-delete="${shipment.id}">Удалить</button>
      </div>
    </article>
  `).join("");
}

async function loadShipments() {
  resetStatus(adminShipmentsStatus, "Загружаем заявки...");
  try {
    const response = await fetch("/api/shipments");
    if (!response.ok) {
      const error = await response.json().catch(() => ({}));
      throw new Error(error.message || `Ошибка ${response.status}`);
    }
    cachedShipments = await response.json();
    renderShipments(cachedShipments);
    resetStatus(adminShipmentsStatus, `Загружено заявок: ${cachedShipments.length}.`);
  } catch (error) {
    resetStatus(adminShipmentsStatus, error.message || "Не удалось загрузить заявки.");
  }
}

async function deleteShipment(id) {
  resetStatus(adminShipmentsStatus, "Удаляем заявку...");
  try {
    const response = await fetch(`/api/shipments/${id}`, { method: "DELETE" });
    if (!response.ok) {
      const error = await response.json().catch(() => ({}));
      throw new Error(error.message || `Ошибка ${response.status}`);
    }
    if (adminShipmentForm.elements.id.value === String(id)) {
      resetShipmentForm();
    }
    await loadShipments();
    resetStatus(adminShipmentsStatus, `Заявка #${id} удалена.`);
  } catch (error) {
    resetStatus(adminShipmentsStatus, error.message || "Не удалось удалить заявку.");
  }
}

function renderVehicles(vehicles) {
  if (!vehicles.length) {
    renderEmpty(adminVehiclesList, "Транспорт пока не найден.");
    return;
  }

  adminVehiclesList.innerHTML = vehicles.map((vehicle) => `
    <article class="user-row">
      <div class="user-row__main">
        <strong>${escapeHtml(vehicle.registrationNumber)}</strong>
        <span>${escapeHtml(`${vehicle.capacityKg} кг`)}</span>
      </div>
      <div class="user-row__meta">
        <span class="user-badge">Carrier</span>
        <span>${escapeHtml(vehicle.carrier ? vehicle.carrier.login : "-")}</span>
      </div>
      <div class="user-row__actions">
        <button class="button button--danger" type="button" data-vehicle-delete="${vehicle.id}">Удалить</button>
      </div>
    </article>
  `).join("");
}

async function loadVehicles() {
  resetStatus(adminVehiclesStatus, "Загружаем транспорт...");
  try {
    const response = await fetch("/api/vehicles");
    if (!response.ok) {
      const error = await response.json().catch(() => ({}));
      throw new Error(error.message || `Ошибка ${response.status}`);
    }
    const vehicles = await response.json();
    renderVehicles(vehicles);
    resetStatus(adminVehiclesStatus, `Загружено транспортных средств: ${vehicles.length}.`);
  } catch (error) {
    resetStatus(adminVehiclesStatus, error.message || "Не удалось загрузить транспорт.");
  }
}

async function deleteVehicle(id) {
  resetStatus(adminVehiclesStatus, "Удаляем транспорт...");
  try {
    const response = await fetch(`/api/vehicles/${id}`, { method: "DELETE" });
    if (!response.ok) {
      const error = await response.json().catch(() => ({}));
      throw new Error(error.message || `Ошибка ${response.status}`);
    }
    await loadVehicles();
    resetStatus(adminVehiclesStatus, `Транспорт #${id} удалён.`);
  } catch (error) {
    resetStatus(adminVehiclesStatus, error.message || "Не удалось удалить транспорт.");
  }
}

function fillCargoForm(cargo) {
  adminCargoForm.elements.id.value = String(cargo.id);
  adminCargoForm.elements.name.value = cargo.name;
  adminCargoForm.elements.weightKg.value = String(cargo.weightKg);
  adminCargoForm.elements.shipmentId.value = String(cargo.shipmentId);
  resetStatus(adminCargoFormStatus, `Редактируется груз #${cargo.id}.`);
}

function resetCargoForm() {
  adminCargoForm.reset();
  adminCargoForm.elements.id.value = "";
  resetStatus(adminCargoFormStatus, "Выберите груз из списка для редактирования.");
}

function renderCargoes(cargoes) {
  if (!cargoes.length) {
    renderEmpty(adminCargoesList, "Грузы пока не найдены.");
    return;
  }

  adminCargoesList.innerHTML = cargoes.map((cargo) => `
    <article class="user-row">
      <div class="user-row__main">
        <strong>${escapeHtml(cargo.name)}</strong>
        <span>${escapeHtml(`${cargo.weightKg} кг`)}</span>
      </div>
      <div class="user-row__meta">
        <span class="user-badge">Shipment</span>
        <span>${escapeHtml(`${cargo.shipmentTrackingNumber} (#${cargo.shipmentId})`)}</span>
      </div>
      <div class="user-row__actions">
        <button class="button button--secondary" type="button" data-cargo-edit='${escapeHtml(JSON.stringify(cargo))}'>Изменить</button>
        <button class="button button--danger" type="button" data-cargo-delete="${cargo.id}">Удалить</button>
      </div>
    </article>
  `).join("");
}

async function loadCargoes() {
  resetStatus(adminCargoesStatus, "Загружаем грузы...");
  try {
    const response = await fetch("/api/cargoes");
    if (!response.ok) {
      const error = await response.json().catch(() => ({}));
      throw new Error(error.message || `Ошибка ${response.status}`);
    }
    const cargoes = await response.json();
    renderCargoes(cargoes);
    resetStatus(adminCargoesStatus, `Загружено грузов: ${cargoes.length}.`);
  } catch (error) {
    resetStatus(adminCargoesStatus, error.message || "Не удалось загрузить грузы.");
  }
}

async function deleteCargo(id) {
  resetStatus(adminCargoesStatus, "Удаляем груз...");
  try {
    const response = await fetch(`/api/cargoes/${id}`, { method: "DELETE" });
    if (!response.ok) {
      const error = await response.json().catch(() => ({}));
      throw new Error(error.message || `Ошибка ${response.status}`);
    }
    if (adminCargoForm.elements.id.value === String(id)) {
      resetCargoForm();
    }
    await loadCargoes();
    await loadShipments();
    resetStatus(adminCargoesStatus, `Груз #${id} удалён.`);
  } catch (error) {
    resetStatus(adminCargoesStatus, error.message || "Не удалось удалить груз.");
  }
}

function updateManagerStats(shipments) {
  const created = shipments.filter((shipment) => shipment.status === "CREATED").length;
  const transit = shipments.filter((shipment) => shipment.status === "IN_TRANSIT").length;
  const delivered = shipments.filter((shipment) => shipment.status === "DELIVERED").length;
  document.querySelector("[data-manager-stat='total']").textContent = String(shipments.length);
  document.querySelector("[data-manager-stat='created']").textContent = String(created);
  document.querySelector("[data-manager-stat='transit']").textContent = String(transit);
  document.querySelector("[data-manager-stat='delivered']").textContent = String(delivered);
}

function fillManagerShipmentForm(shipment) {
  managerShipmentForm.elements.id.value = String(shipment.id);
  managerShipmentForm.elements.trackingNumber.value = shipment.trackingNumber;
  managerShipmentForm.elements.originCity.value = shipment.originCity;
  managerShipmentForm.elements.destinationCity.value = shipment.destinationCity;
  managerShipmentForm.elements.status.value = shipment.status;
  managerShipmentForm.elements.customerId.value = String(shipment.customer.id);
  managerShipmentForm.elements.managerId.value = String(shipment.manager.id);
  const selectedManagerVehicleIds = new Set(shipment.vehicles.map((vehicle) => String(vehicle.id)));
  Array.from(managerShipmentForm.elements.vehicleIds.options).forEach((option) => {
    option.selected = selectedManagerVehicleIds.has(option.value);
  });
  managerShipmentForm.elements.cargoName.value = shipment.cargoes[0] ? shipment.cargoes[0].name : "";
  managerShipmentForm.elements.cargoWeightKg.value = shipment.cargoes[0] ? String(shipment.cargoes[0].weightKg) : "";
  managerShipmentForm.elements.orderCreatedAt.value = formatDateTimeForInput(shipment.schedule.orderCreatedAt);
  managerShipmentForm.elements.orderReceivedAt.value = formatDateTimeForInput(shipment.schedule.orderReceivedAt);
  managerShipmentForm.elements.arrivalAt.value = formatDateTimeForInput(shipment.schedule.arrivalAt);
  resetStatus(managerShipmentFormStatus, `Редактируется заявка #${shipment.id}.`);
}

function resetManagerShipmentForm() {
  if (!managerShipmentForm) {
    return;
  }
  managerShipmentForm.reset();
  managerShipmentForm.elements.id.value = "";
  resetStatus(managerShipmentFormStatus, "Заполните форму для новой заявки или выберите существующую.");
}

function renderManagerShipments(shipments) {
  if (!shipments.length) {
    renderEmpty(managerShipmentsList, "Заявки пока не найдены.");
    updateManagerStats(shipments);
    return;
  }

  managerShipmentsList.innerHTML = shipments.map((shipment) => `
    <article class="user-row">
      <div class="user-row__main">
        <strong>${escapeHtml(shipment.trackingNumber)}</strong>
        <span>${escapeHtml(`${shipment.originCity} -> ${shipment.destinationCity}`)}</span>
      </div>
      <div class="user-row__meta">
        <span class="user-badge">${escapeHtml(shipment.status)}</span>
        <span>Клиент: ${escapeHtml(shipment.customer.login)}</span>
      </div>
      <div class="user-row__actions">
        <button class="button button--secondary" type="button" data-manager-shipment-edit="${shipment.id}">Изменить</button>
        <button class="button button--danger" type="button" data-manager-shipment-delete="${shipment.id}">Удалить</button>
      </div>
    </article>
  `).join("");

  updateManagerStats(shipments);
}

async function loadManagerShipments() {
  resetStatus(managerShipmentsStatus, "Загружаем заявки...");
  try {
    const response = await fetch("/api/shipments");
    if (!response.ok) {
      const error = await response.json().catch(() => ({}));
      throw new Error(error.message || `Ошибка ${response.status}`);
    }
    cachedShipments = await response.json();
    renderManagerShipments(cachedShipments);
    resetStatus(managerShipmentsStatus, `Загружено заявок: ${cachedShipments.length}.`);
  } catch (error) {
    resetStatus(managerShipmentsStatus, error.message || "Не удалось загрузить заявки.");
  }
}

async function createManagerShipment() {
  resetStatus(managerShipmentFormStatus, "Создаём заявку...");
  const cargoName = managerShipmentForm.elements.cargoName.value.trim();
  const cargoWeightKg = managerShipmentForm.elements.cargoWeightKg.value;
  if (!cargoName || !cargoWeightKg) {
    resetStatus(managerShipmentFormStatus, "Для новой заявки заполните название и вес груза.");
    return;
  }
  const payload = {
    trackingNumber: managerShipmentForm.elements.trackingNumber.value,
    originCity: managerShipmentForm.elements.originCity.value,
    destinationCity: managerShipmentForm.elements.destinationCity.value,
    status: managerShipmentForm.elements.status.value,
    customerId: Number(managerShipmentForm.elements.customerId.value),
    managerId: Number(managerShipmentForm.elements.managerId.value),
    vehicleIds: getSelectedNumericValues(managerShipmentForm.elements.vehicleIds),
    cargoes: [
      {
        name: cargoName,
        weightKg: Number(cargoWeightKg)
      }
    ],
    schedule: {
      orderCreatedAt: managerShipmentForm.elements.orderCreatedAt.value,
      orderReceivedAt: managerShipmentForm.elements.orderReceivedAt.value,
      arrivalAt: managerShipmentForm.elements.arrivalAt.value
    }
  };
  try {
    const response = await fetch("/api/shipments", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload)
    });
    if (!response.ok) {
      const error = await response.json().catch(() => ({}));
      throw new Error(error.message || `Ошибка ${response.status}`);
    }
    await loadManagerShipments();
    await loadManagerCargoes();
    resetManagerShipmentForm();
    resetStatus(managerShipmentFormStatus, "Заявка создана.");
  } catch (error) {
    resetStatus(managerShipmentFormStatus, error.message || "Не удалось создать заявку.");
  }
}

async function updateManagerShipment() {
  const shipmentId = managerShipmentForm.elements.id.value;
  if (!shipmentId) {
    resetStatus(managerShipmentFormStatus, "Сначала выберите заявку из списка.");
    return;
  }
  const existingShipment = cachedShipments.find((item) => String(item.id) === shipmentId);
  if (!existingShipment) {
    resetStatus(managerShipmentFormStatus, "Не удалось найти исходные данные заявки.");
    return;
  }
  resetStatus(managerShipmentFormStatus, "Сохраняем изменения...");
  const payload = {
    trackingNumber: managerShipmentForm.elements.trackingNumber.value,
    originCity: managerShipmentForm.elements.originCity.value,
    destinationCity: managerShipmentForm.elements.destinationCity.value,
    status: managerShipmentForm.elements.status.value,
    customerId: Number(managerShipmentForm.elements.customerId.value),
    managerId: Number(managerShipmentForm.elements.managerId.value),
    vehicleIds: getSelectedNumericValues(managerShipmentForm.elements.vehicleIds),
    cargoes: existingShipment.cargoes.map((cargo) => ({
      name: cargo.name,
      weightKg: cargo.weightKg
    })),
    schedule: {
      orderCreatedAt: managerShipmentForm.elements.orderCreatedAt.value,
      orderReceivedAt: managerShipmentForm.elements.orderReceivedAt.value,
      arrivalAt: managerShipmentForm.elements.arrivalAt.value
    }
  };
  try {
    const response = await fetch(`/api/shipments/${shipmentId}`, {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload)
    });
    if (!response.ok) {
      const error = await response.json().catch(() => ({}));
      throw new Error(error.message || `Ошибка ${response.status}`);
    }
    await loadManagerShipments();
    resetStatus(managerShipmentFormStatus, `Заявка #${shipmentId} обновлена.`);
  } catch (error) {
    resetStatus(managerShipmentFormStatus, error.message || "Не удалось обновить заявку.");
  }
}

async function deleteManagerShipment(id) {
  resetStatus(managerShipmentsStatus, "Удаляем заявку...");
  try {
    const response = await fetch(`/api/shipments/${id}`, { method: "DELETE" });
    if (!response.ok) {
      const error = await response.json().catch(() => ({}));
      throw new Error(error.message || `Ошибка ${response.status}`);
    }
    if (managerShipmentForm.elements.id.value === String(id)) {
      resetManagerShipmentForm();
    }
    await loadManagerShipments();
    await loadManagerCargoes();
    resetStatus(managerShipmentsStatus, `Заявка #${id} удалена.`);
  } catch (error) {
    resetStatus(managerShipmentsStatus, error.message || "Не удалось удалить заявку.");
  }
}

function renderManagerVehicles(vehicles) {
  if (!vehicles.length) {
    renderEmpty(managerVehiclesList, "Транспорт пока не найден.");
    return;
  }
  managerVehiclesList.innerHTML = vehicles.map((vehicle) => `
    <article class="user-row">
      <div class="user-row__main">
        <strong>${escapeHtml(vehicle.registrationNumber)}</strong>
        <span>${escapeHtml(`${vehicle.capacityKg} кг`)}</span>
      </div>
      <div class="user-row__meta">
        <span class="user-badge">Carrier</span>
        <span>${escapeHtml(vehicle.carrier ? vehicle.carrier.login : "-")}</span>
      </div>
    </article>
  `).join("");
}

async function loadManagerVehicles() {
  resetStatus(managerVehiclesStatus, "Загружаем транспорт...");
  try {
    const response = await fetch("/api/vehicles");
    if (!response.ok) {
      const error = await response.json().catch(() => ({}));
      throw new Error(error.message || `Ошибка ${response.status}`);
    }
    const vehicles = await response.json();
    renderManagerVehicles(vehicles);
    resetStatus(managerVehiclesStatus, `Загружено транспортных средств: ${vehicles.length}.`);
  } catch (error) {
    resetStatus(managerVehiclesStatus, error.message || "Не удалось загрузить транспорт.");
  }
}

function renderManagerCargoes(cargoes) {
  if (!cargoes.length) {
    renderEmpty(managerCargoesList, "Грузы пока не найдены.");
    return;
  }
  managerCargoesList.innerHTML = cargoes.map((cargo) => `
    <article class="user-row">
      <div class="user-row__main">
        <strong>${escapeHtml(cargo.name)}</strong>
        <span>${escapeHtml(`${cargo.weightKg} кг`)}</span>
      </div>
      <div class="user-row__meta">
        <span class="user-badge">Shipment</span>
        <span>${escapeHtml(`${cargo.shipmentTrackingNumber} (#${cargo.shipmentId})`)}</span>
      </div>
    </article>
  `).join("");
}

async function loadManagerCargoes() {
  resetStatus(managerCargoesStatus, "Загружаем грузы...");
  try {
    const response = await fetch("/api/cargoes");
    if (!response.ok) {
      const error = await response.json().catch(() => ({}));
      throw new Error(error.message || `Ошибка ${response.status}`);
    }
    const cargoes = await response.json();
    renderManagerCargoes(cargoes);
    resetStatus(managerCargoesStatus, `Загружено грузов: ${cargoes.length}.`);
  } catch (error) {
    resetStatus(managerCargoesStatus, error.message || "Не удалось загрузить грузы.");
  }
}

if (pageType === "admin") {
  resetAdminUserForm();
  resetShipmentForm();
  resetCargoForm();
  loadUsers();
  loadShipments();
  loadVehicles();
  loadCargoes();
  hydrateShipmentFormOptions(adminShipmentForm, adminShipmentFormStatus);

  refreshUsersButton?.addEventListener("click", loadUsers);
  refreshShipmentsButton?.addEventListener("click", loadShipments);
  refreshVehiclesButton?.addEventListener("click", loadVehicles);
  refreshCargoesButton?.addEventListener("click", loadCargoes);
  adminUserFormReset?.addEventListener("click", resetAdminUserForm);
  adminShipmentFormReset?.addEventListener("click", resetShipmentForm);
  adminCargoFormReset?.addEventListener("click", resetCargoForm);

  adminUsersList?.addEventListener("click", async (event) => {
    const target = event.target;
    if (!(target instanceof HTMLElement)) {
      return;
    }
    const editButton = target.closest("[data-user-edit]");
    if (editButton instanceof HTMLButtonElement) {
      fillAdminUserForm(JSON.parse(editButton.dataset.userEdit || "{}"));
      return;
    }
    const deleteButton = target.closest("[data-user-delete]");
    if (deleteButton instanceof HTMLButtonElement) {
      if (window.confirm("Удалить пользователя? Это действие нельзя отменить.")) {
        await deleteUser(deleteButton.dataset.userDelete);
      }
    }
  });

  adminShipmentsList?.addEventListener("click", async (event) => {
    const target = event.target;
    if (!(target instanceof HTMLElement)) {
      return;
    }
    const editButton = target.closest("[data-shipment-edit]");
    if (editButton instanceof HTMLButtonElement) {
      const shipment = cachedShipments.find((item) => String(item.id) === editButton.dataset.shipmentEdit);
      if (shipment) {
        fillShipmentForm(shipment);
      }
      return;
    }
    const deleteButton = target.closest("[data-shipment-delete]");
    if (deleteButton instanceof HTMLButtonElement) {
      if (window.confirm("Удалить заявку? Это действие нельзя отменить.")) {
        await deleteShipment(deleteButton.dataset.shipmentDelete);
      }
    }
  });

  adminVehiclesList?.addEventListener("click", async (event) => {
    const target = event.target;
    if (!(target instanceof HTMLElement)) {
      return;
    }
    const deleteButton = target.closest("[data-vehicle-delete]");
    if (deleteButton instanceof HTMLButtonElement) {
      if (window.confirm("Удалить транспортное средство?")) {
        await deleteVehicle(deleteButton.dataset.vehicleDelete);
      }
    }
  });

  adminCargoesList?.addEventListener("click", async (event) => {
    const target = event.target;
    if (!(target instanceof HTMLElement)) {
      return;
    }
    const editButton = target.closest("[data-cargo-edit]");
    if (editButton instanceof HTMLButtonElement) {
      fillCargoForm(JSON.parse(editButton.dataset.cargoEdit || "{}"));
      return;
    }
    const deleteButton = target.closest("[data-cargo-delete]");
    if (deleteButton instanceof HTMLButtonElement) {
      if (window.confirm("Удалить груз?")) {
        await deleteCargo(deleteButton.dataset.cargoDelete);
      }
    }
  });

  adminUserForm?.addEventListener("submit", async (event) => {
    event.preventDefault();
    const userId = adminUserForm.elements.id.value;
    if (!userId) {
      resetStatus(adminUserFormStatus, "Сначала выберите пользователя из списка.");
      return;
    }
    resetStatus(adminUserFormStatus, "Сохраняем изменения...");
    const formData = new FormData(adminUserForm);
    const payload = Object.fromEntries(formData.entries());
    payload.email = String(payload.email).trim().toLowerCase();
    payload.login = String(payload.login).trim().toLowerCase();
    try {
      const response = await fetch(`/api/users/${userId}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
      });
      if (!response.ok) {
        const error = await response.json().catch(() => ({}));
        throw new Error(error.message || `Ошибка ${response.status}`);
      }
      const user = await response.json();
      fillAdminUserForm(user);
      await loadUsers();
      resetStatus(adminUserFormStatus, `Пользователь ${user.login} обновлён.`);
    } catch (error) {
      resetStatus(adminUserFormStatus, error.message || "Не удалось сохранить изменения.");
    }
  });

  adminShipmentForm?.addEventListener("submit", async (event) => {
    event.preventDefault();
    const shipmentId = adminShipmentForm.elements.id.value;
    if (!shipmentId) {
      resetStatus(adminShipmentFormStatus, "Сначала выберите заявку из списка.");
      return;
    }
    const existingShipment = cachedShipments.find((item) => String(item.id) === shipmentId);
    if (!existingShipment) {
      resetStatus(adminShipmentFormStatus, "Не удалось найти исходные данные заявки.");
      return;
    }
    resetStatus(adminShipmentFormStatus, "Сохраняем изменения...");
    const payload = {
      trackingNumber: adminShipmentForm.elements.trackingNumber.value,
      originCity: adminShipmentForm.elements.originCity.value,
      destinationCity: adminShipmentForm.elements.destinationCity.value,
      status: adminShipmentForm.elements.status.value,
      customerId: Number(adminShipmentForm.elements.customerId.value),
      managerId: Number(adminShipmentForm.elements.managerId.value),
      vehicleIds: getSelectedNumericValues(adminShipmentForm.elements.vehicleIds),
      cargoes: existingShipment.cargoes.map((cargo) => ({
        name: cargo.name,
        weightKg: cargo.weightKg
      })),
      schedule: {
        orderCreatedAt: adminShipmentForm.elements.orderCreatedAt.value,
        orderReceivedAt: adminShipmentForm.elements.orderReceivedAt.value,
        arrivalAt: adminShipmentForm.elements.arrivalAt.value
      }
    };
    try {
      const response = await fetch(`/api/shipments/${shipmentId}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
      });
      if (!response.ok) {
        const error = await response.json().catch(() => ({}));
        throw new Error(error.message || `Ошибка ${response.status}`);
      }
      await loadShipments();
      await loadCargoes();
      resetStatus(adminShipmentFormStatus, `Заявка #${shipmentId} обновлена.`);
    } catch (error) {
      resetStatus(adminShipmentFormStatus, error.message || "Не удалось сохранить изменения.");
    }
  });

  adminCargoForm?.addEventListener("submit", async (event) => {
    event.preventDefault();
    const cargoId = adminCargoForm.elements.id.value;
    if (!cargoId) {
      resetStatus(adminCargoFormStatus, "Сначала выберите груз из списка.");
      return;
    }
    resetStatus(adminCargoFormStatus, "Сохраняем изменения...");
    const payload = {
      name: adminCargoForm.elements.name.value,
      weightKg: Number(adminCargoForm.elements.weightKg.value),
      shipmentId: Number(adminCargoForm.elements.shipmentId.value)
    };
    try {
      const response = await fetch(`/api/cargoes/${cargoId}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
      });
      if (!response.ok) {
        const error = await response.json().catch(() => ({}));
        throw new Error(error.message || `Ошибка ${response.status}`);
      }
      await loadCargoes();
      await loadShipments();
      resetStatus(adminCargoFormStatus, `Груз #${cargoId} обновлён.`);
    } catch (error) {
      resetStatus(adminCargoFormStatus, error.message || "Не удалось сохранить изменения.");
    }
  });
}

if (pageType === "manager") {
  resetManagerShipmentForm();
  loadManagerShipments();
  loadManagerVehicles();
  loadManagerCargoes();
  hydrateShipmentFormOptions(managerShipmentForm, managerShipmentFormStatus);

  refreshManagerShipmentsButton?.addEventListener("click", loadManagerShipments);
  refreshManagerVehiclesButton?.addEventListener("click", loadManagerVehicles);
  refreshManagerCargoesButton?.addEventListener("click", loadManagerCargoes);
  managerShipmentFormReset?.addEventListener("click", resetManagerShipmentForm);
  managerShipmentCreate?.addEventListener("click", async () => {
    managerShipmentForm.elements.id.value = "";
    await createManagerShipment();
  });

  managerShipmentsList?.addEventListener("click", async (event) => {
    const target = event.target;
    if (!(target instanceof HTMLElement)) {
      return;
    }
    const editButton = target.closest("[data-manager-shipment-edit]");
    if (editButton instanceof HTMLButtonElement) {
      const shipment = cachedShipments.find(
          (item) => String(item.id) === editButton.dataset.managerShipmentEdit
      );
      if (shipment) {
        fillManagerShipmentForm(shipment);
      }
      return;
    }
    const deleteButton = target.closest("[data-manager-shipment-delete]");
    if (deleteButton instanceof HTMLButtonElement) {
      if (window.confirm("Удалить заявку? Это действие нельзя отменить.")) {
        await deleteManagerShipment(deleteButton.dataset.managerShipmentDelete);
      }
    }
  });

  managerShipmentForm?.addEventListener("submit", async (event) => {
    event.preventDefault();
    if (managerShipmentForm.elements.id.value) {
      await updateManagerShipment();
      return;
    }
    await createManagerShipment();
  });
}

function updateCustomerStats(shipments) {
  const created = shipments.filter((shipment) => shipment.status === "CREATED").length;
  const transit = shipments.filter((shipment) => shipment.status === "IN_TRANSIT").length;
  const delivered = shipments.filter((shipment) => shipment.status === "DELIVERED").length;
  document.querySelector("[data-customer-stat='total']").textContent = String(shipments.length);
  document.querySelector("[data-customer-stat='created']").textContent = String(created);
  document.querySelector("[data-customer-stat='transit']").textContent = String(transit);
  document.querySelector("[data-customer-stat='delivered']").textContent = String(delivered);
}

function renderCustomerShipments(shipments) {
  if (!shipments.length) {
    renderEmpty(customerShipmentsList, "У вас пока нет заявок.");
    updateCustomerStats(shipments);
    return;
  }

  customerShipmentsList.innerHTML = shipments.map((shipment) => `
    <article class="user-row">
      <div class="user-row__main">
        <strong>${escapeHtml(shipment.trackingNumber)}</strong>
        <span>${escapeHtml(`${shipment.originCity} -> ${shipment.destinationCity}`)}</span>
      </div>
      <div class="user-row__meta">
        <span class="user-badge">${escapeHtml(shipment.status)}</span>
        <span>Менеджер: ${escapeHtml(shipment.manager ? shipment.manager.login : "-")}</span>
      </div>
      <div class="user-row__meta">
        <span>Грузов: ${shipment.cargoes.length}</span>
        <span>Транспорта: ${shipment.vehicles.length}</span>
      </div>
    </article>
  `).join("");

  updateCustomerStats(shipments);
}

async function loadCustomerShipments() {
  const currentUser = getCurrentUser();
  if (!currentUser) {
    resetStatus(customerShipmentsStatus, "Сначала выполните вход в систему.");
    renderEmpty(customerShipmentsList, "Нет данных пользователя для показа заявок.");
    updateCustomerStats([]);
    return;
  }

  resetStatus(customerShipmentsStatus, "Загружаем ваши заявки...");
  try {
    const response = await fetch("/api/shipments");
    if (!response.ok) {
      const error = await response.json().catch(() => ({}));
      throw new Error(error.message || `Ошибка ${response.status}`);
    }
    const shipments = await response.json();
    const customerShipments = shipments.filter(
        (shipment) => shipment.customer && shipment.customer.id === currentUser.id
    );
    renderCustomerShipments(customerShipments);
    resetStatus(customerShipmentsStatus, `Найдено ваших заявок: ${customerShipments.length}.`);
  } catch (error) {
    resetStatus(customerShipmentsStatus, error.message || "Не удалось загрузить заявки.");
  }
}

if (pageType === "customer") {
  loadCustomerShipments();
  refreshCustomerShipmentsButton?.addEventListener("click", loadCustomerShipments);
}

function updateCarrierStats(shipments) {
  const created = shipments.filter((shipment) => shipment.status === "CREATED").length;
  const transit = shipments.filter((shipment) => shipment.status === "IN_TRANSIT").length;
  const delivered = shipments.filter((shipment) => shipment.status === "DELIVERED").length;
  document.querySelector("[data-carrier-stat='total']").textContent = String(shipments.length);
  document.querySelector("[data-carrier-stat='created']").textContent = String(created);
  document.querySelector("[data-carrier-stat='transit']").textContent = String(transit);
  document.querySelector("[data-carrier-stat='delivered']").textContent = String(delivered);
}

function resetCarrierVehicleForm() {
  if (!carrierVehicleForm) {
    return;
  }
  const currentUser = getCurrentUser();
  carrierVehicleForm.reset();
  carrierVehicleForm.elements.id.value = "";
  carrierVehicleForm.elements.carrierId.value = currentUser ? String(currentUser.id) : "";
  carrierVehicleForm.elements.carrierDisplay.value = currentUser
    ? `${currentUser.firstName} ${currentUser.lastName} (${currentUser.login})`
    : "";
  resetStatus(carrierVehicleFormStatus, "Заполните форму для нового транспорта или выберите существующий.");
}

function fillCarrierVehicleForm(vehicle) {
  carrierVehicleForm.elements.id.value = String(vehicle.id);
  carrierVehicleForm.elements.registrationNumber.value = vehicle.registrationNumber;
  carrierVehicleForm.elements.capacityKg.value = String(vehicle.capacityKg);
  carrierVehicleForm.elements.carrierId.value = String(vehicle.carrier ? vehicle.carrier.id : "");
  carrierVehicleForm.elements.carrierDisplay.value = vehicle.carrier
    ? `${vehicle.carrier.firstName} ${vehicle.carrier.lastName} (${vehicle.carrier.login})`
    : "";
  resetStatus(carrierVehicleFormStatus, `Редактируется транспорт #${vehicle.id}.`);
}

function renderCarrierShipments(shipments) {
  if (!shipments.length) {
    renderEmpty(carrierShipmentsList, "У вас пока нет заявок.");
    updateCarrierStats(shipments);
    return;
  }

  carrierShipmentsList.innerHTML = shipments.map((shipment) => `
    <article class="user-row">
      <div class="user-row__main">
        <strong>${escapeHtml(shipment.trackingNumber)}</strong>
        <span>${escapeHtml(`${shipment.originCity} -> ${shipment.destinationCity}`)}</span>
      </div>
      <div class="user-row__meta">
        <span class="user-badge">${escapeHtml(shipment.status)}</span>
        <span>Менеджер: ${escapeHtml(shipment.manager ? shipment.manager.login : "-")}</span>
      </div>
      <div class="user-row__meta">
        <span>Грузов: ${shipment.cargoes.length}</span>
        <span>Транспорта: ${shipment.vehicles.length}</span>
      </div>
    </article>
  `).join("");

  updateCarrierStats(shipments);
}

async function loadCarrierShipments() {
  const currentUser = getCurrentUser();
  if (!currentUser) {
    resetStatus(carrierShipmentsStatus, "Сначала выполните вход в систему.");
    renderEmpty(carrierShipmentsList, "Нет данных пользователя для показа заявок.");
    updateCarrierStats([]);
    return;
  }

  resetStatus(carrierShipmentsStatus, "Загружаем ваши заявки...");
  try {
    const response = await fetch("/api/shipments");
    if (!response.ok) {
      const error = await response.json().catch(() => ({}));
      throw new Error(error.message || `Ошибка ${response.status}`);
    }
    const shipments = await response.json();
    const carrierShipments = shipments.filter((shipment) =>
      shipment.vehicles.some((vehicle) => vehicle.carrier && vehicle.carrier.id === currentUser.id)
    );
    renderCarrierShipments(carrierShipments);
    resetStatus(carrierShipmentsStatus, `Найдено ваших заявок: ${carrierShipments.length}.`);
  } catch (error) {
    resetStatus(carrierShipmentsStatus, error.message || "Не удалось загрузить заявки.");
  }
}

function renderCarrierVehicles(vehicles) {
  if (!vehicles.length) {
    renderEmpty(carrierVehiclesList, "У вас пока нет транспорта.");
    return;
  }

  carrierVehiclesList.innerHTML = vehicles.map((vehicle) => `
    <article class="user-row">
      <div class="user-row__main">
        <strong>${escapeHtml(vehicle.registrationNumber)}</strong>
        <span>${escapeHtml(`${vehicle.capacityKg} кг`)}</span>
      </div>
      <div class="user-row__meta">
        <span class="user-badge">Carrier</span>
        <span>${escapeHtml(vehicle.carrier ? vehicle.carrier.login : "-")}</span>
      </div>
      <div class="user-row__actions">
        <button class="button button--secondary" type="button" data-carrier-vehicle-edit='${escapeHtml(JSON.stringify(vehicle))}'>Изменить</button>
      </div>
    </article>
  `).join("");
}

async function loadCarrierVehicles() {
  const currentUser = getCurrentUser();
  if (!currentUser) {
    resetStatus(carrierVehiclesStatus, "Сначала выполните вход в систему.");
    renderEmpty(carrierVehiclesList, "Нет данных пользователя для показа транспорта.");
    return;
  }

  resetStatus(carrierVehiclesStatus, "Загружаем ваш транспорт...");
  try {
    const response = await fetch("/api/vehicles");
    if (!response.ok) {
      const error = await response.json().catch(() => ({}));
      throw new Error(error.message || `Ошибка ${response.status}`);
    }
    const vehicles = await response.json();
    const carrierVehicles = vehicles.filter(
        (vehicle) => vehicle.carrier && vehicle.carrier.id === currentUser.id
    );
    renderCarrierVehicles(carrierVehicles);
    resetStatus(carrierVehiclesStatus, `Найдено транспортных средств: ${carrierVehicles.length}.`);
  } catch (error) {
    resetStatus(carrierVehiclesStatus, error.message || "Не удалось загрузить транспорт.");
  }
}

function renderCarrierCargoes(cargoes) {
  if (!cargoes.length) {
    renderEmpty(carrierCargoesList, "По вашим заявкам грузы пока не найдены.");
    return;
  }

  carrierCargoesList.innerHTML = cargoes.map((cargo) => `
    <article class="user-row">
      <div class="user-row__main">
        <strong>${escapeHtml(cargo.name)}</strong>
        <span>${escapeHtml(`${cargo.weightKg} кг`)}</span>
      </div>
      <div class="user-row__meta">
        <span class="user-badge">Shipment</span>
        <span>${escapeHtml(`${cargo.shipmentTrackingNumber} (#${cargo.shipmentId})`)}</span>
      </div>
    </article>
  `).join("");
}

async function loadCarrierCargoes() {
  const currentUser = getCurrentUser();
  if (!currentUser) {
    resetStatus(carrierCargoesStatus, "Сначала выполните вход в систему.");
    renderEmpty(carrierCargoesList, "Нет данных пользователя для показа грузов.");
    return;
  }

  resetStatus(carrierCargoesStatus, "Загружаем грузы по вашим заявкам...");
  try {
    const [shipmentsResponse, cargoesResponse] = await Promise.all([
      fetch("/api/shipments"),
      fetch("/api/cargoes")
    ]);
    if (!shipmentsResponse.ok || !cargoesResponse.ok) {
      throw new Error("Не удалось загрузить данные для перевозчика.");
    }
    const shipments = await shipmentsResponse.json();
    const cargoes = await cargoesResponse.json();
    const carrierShipmentIds = new Set(
        shipments
            .filter((shipment) =>
              shipment.vehicles.some((vehicle) => vehicle.carrier && vehicle.carrier.id === currentUser.id)
            )
            .map((shipment) => shipment.id)
    );
    const carrierCargoes = cargoes.filter((cargo) => carrierShipmentIds.has(cargo.shipmentId));
    renderCarrierCargoes(carrierCargoes);
    resetStatus(carrierCargoesStatus, `Найдено грузов: ${carrierCargoes.length}.`);
  } catch (error) {
    resetStatus(carrierCargoesStatus, error.message || "Не удалось загрузить грузы.");
  }
}

if (pageType === "carrier") {
  resetCarrierVehicleForm();
  loadCarrierShipments();
  loadCarrierVehicles();
  loadCarrierCargoes();

  refreshCarrierShipmentsButton?.addEventListener("click", loadCarrierShipments);
  refreshCarrierVehiclesButton?.addEventListener("click", loadCarrierVehicles);
  refreshCarrierCargoesButton?.addEventListener("click", loadCarrierCargoes);
  carrierVehicleFormReset?.addEventListener("click", resetCarrierVehicleForm);

  carrierVehiclesList?.addEventListener("click", (event) => {
    const target = event.target;
    if (!(target instanceof HTMLElement)) {
      return;
    }
    const editButton = target.closest("[data-carrier-vehicle-edit]");
    if (editButton instanceof HTMLButtonElement) {
      fillCarrierVehicleForm(JSON.parse(editButton.dataset.carrierVehicleEdit || "{}"));
    }
  });

  carrierVehicleForm?.addEventListener("submit", async (event) => {
    event.preventDefault();
    const vehicleId = carrierVehicleForm.elements.id.value;
    resetStatus(carrierVehicleFormStatus, "Сохраняем изменения...");
    const payload = {
      registrationNumber: carrierVehicleForm.elements.registrationNumber.value,
      capacityKg: Number(carrierVehicleForm.elements.capacityKg.value),
      carrierId: Number(carrierVehicleForm.elements.carrierId.value)
    };

    try {
      const response = await fetch(vehicleId ? `/api/vehicles/${vehicleId}` : "/api/vehicles", {
        method: vehicleId ? "PUT" : "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify(payload)
      });
      if (!response.ok) {
        const error = await response.json().catch(() => ({}));
        throw new Error(error.message || `Ошибка ${response.status}`);
      }
      const vehicle = await response.json();
      fillCarrierVehicleForm(vehicle);
      await loadCarrierVehicles();
      await loadCarrierShipments();
      await loadCarrierCargoes();
      resetStatus(
          carrierVehicleFormStatus,
          vehicleId ? `Транспорт #${vehicleId} обновлён.` : `Транспорт #${vehicle.id} создан.`
      );
    } catch (error) {
      resetStatus(carrierVehicleFormStatus, error.message || "Не удалось сохранить изменения.");
    }
  });

  carrierVehicleCreate?.addEventListener("click", () => {
    resetCarrierVehicleForm();
  });
}
