-- Run once in PostgreSQL after deploying the new code.
-- Take a backup before executing.

-- 1) Migrate CLICK rows to unified table
INSERT INTO payment_transactions (
    id,
    created_at,
    updated_at,
    visibility,
    payment_method,
    provider_transaction_id,
    merchant_transaction_id,
    user_id,
    amount,
    click_prepare_time,
    click_complete_time,
    click_cancel_time,
    click_status,
    wallet_transaction_id
)
SELECT
    ct.id,
    COALESCE(ct.created_at, NOW()),
    COALESCE(ct.updated_at, NOW()),
    COALESCE(ct.visibility, TRUE),
    'CLICK',
    ct.id::text,
    ct.user_id,
    ct.user_id,
    ct.amount,
    ct.prepare_time,
    ct.complete_time,
    ct.cancel_time,
    ct.status,
    ct.wallet_transaction_id
FROM click_transaction ct
WHERE NOT EXISTS (
    SELECT 1
    FROM payment_transactions pt
    WHERE pt.payment_method = 'CLICK'
      AND pt.provider_transaction_id = ct.id::text
);

-- 2) Migrate PAYME rows to unified table
INSERT INTO payment_transactions (
    created_at,
    updated_at,
    visibility,
    payment_method,
    provider_transaction_id,
    merchant_transaction_id,
    user_id,
    amount,
    create_time,
    perform_time,
    cancel_time,
    payme_state,
    reason,
    wallet_transaction_id
)
SELECT
    NOW(),
    NOW(),
    TRUE,
    'PAYME',
    pt.id,
    pt.user_id,
    pt.user_id,
    pt.amount,
    pt.create_time,
    pt.perform_time,
    pt.cancel_time,
    pt.state,
    pt.reason,
    pt.wallet_transaction_id
FROM payme_transactions pt
WHERE NOT EXISTS (
    SELECT 1
    FROM payment_transactions p
    WHERE p.payment_method = 'PAYME'
      AND p.provider_transaction_id = pt.id
);

-- 3) Sync sequence if CLICK ids were copied as primary keys
SELECT setval(
    pg_get_serial_sequence('payment_transactions', 'id'),
    COALESCE((SELECT MAX(id) FROM payment_transactions), 1),
    true
);

-- 4) Drop old tables (only after data verification)
DROP TABLE IF EXISTS click_transaction;
DROP TABLE IF EXISTS payme_transactions;
