-- Runs once on first container start (when the data volume is empty).
-- Creates the two application databases if they don't already exist.

SELECT 'CREATE DATABASE fintrack_auth'
  WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'fintrack_auth')\gexec

SELECT 'CREATE DATABASE fintrack_planning'
  WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'fintrack_planning')\gexec
