(ns fhirterm.fhir.core
  (:require [fhirterm.fhir.operation-outcome :as oo]
            [fhirterm.fhir.parameters :as params]))

(def make-operation-outcome oo/make)
(def make-parameters params/make)
