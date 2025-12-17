-- 1. Switch to the Pluggable Database (PDB)
-- Your logs confirmed the name is FREEPDB1
ALTER SESSION SET CONTAINER = FREEPDB1;

-- 2. Create the 'sa' user
-- We use a basic block to handle the case where the user might already exist
BEGIN
EXECUTE IMMEDIATE 'DROP USER sa CASCADE';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE != -1918 THEN
            RAISE;
END IF;
END;
/

CREATE USER sa IDENTIFIED BY sa;

-- 3. Grant necessary permissions for a Spring Boot application
GRANT CONNECT, RESOURCE, DBA TO sa;
GRANT CREATE SESSION TO sa;
GRANT UNLIMITED TABLESPACE TO sa;

-- 4. Final verification settings
ALTER USER sa ACCOUNT UNLOCK;

COMMIT;