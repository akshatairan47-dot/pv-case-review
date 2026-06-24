import { useCase } from './hooks/useCase'
import { Header } from './components/Header'
import { SectionGroup } from './components/SectionGroup'
import { MissingFieldsBanner } from './components/MissingFieldsBanner'
import { OfflineBanner } from './components/OfflineBanner'
import './App.css'

const CASE_ID = 'PV-2026-0451'

function App() {
  const { data: pvCase, loading, error, isOffline } = useCase(CASE_ID)

  return (
    <>
      <Header
        key={pvCase?.case_id ?? 'pending'}
        caseId={pvCase?.case_id ?? CASE_ID}
        initialClassification={pvCase?.case_classification ?? ''}
      />

      {isOffline && error && <OfflineBanner reason={error.message} />}
      {pvCase && <MissingFieldsBanner missingFields={pvCase.missing_fields} />}

      <section id="case-review">
        {loading && (
          <div className="case-review__loading">
            <span className="spinner" aria-hidden="true" />
            <p>Loading case {CASE_ID}...</p>
          </div>
        )}

        {pvCase &&
          (Object.keys(pvCase.sections).length === 0 ? (
            <p className="case-review__empty">This case has no sections to review.</p>
          ) : (
            <div className="case-review__sections">
              {Object.entries(pvCase.sections).map(([sectionName, fields]) => (
                <SectionGroup
                  key={sectionName}
                  name={sectionName}
                  fields={fields}
                  caseId={pvCase.case_id}
                />
              ))}
            </div>
          ))}
      </section>

      <div className="ticks"></div>
      <section id="spacer"></section>
    </>
  )
}

export default App
