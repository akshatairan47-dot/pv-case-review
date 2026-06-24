import { useCase } from './hooks/useCase'
import { Header } from './components/Header'
import { SectionGroup } from './components/SectionGroup'
import './App.css'

const CASE_ID = 'PV-2026-0451'

function App() {
  const { data: pvCase, loading, error } = useCase(CASE_ID)

  return (
    <>
      <Header
        key={pvCase?.case_id ?? 'pending'}
        caseId={pvCase?.case_id ?? CASE_ID}
        initialClassification={pvCase?.case_classification ?? ''}
      />

      <section id="case-review">
        {loading && <p>Loading case {CASE_ID}...</p>}
        {error && <p>Failed to load case {CASE_ID}: {error.message}</p>}
        {pvCase && (
          <div className="case-review__sections">
            {Object.entries(pvCase.sections).map(([sectionName, fields]) => (
              <SectionGroup key={sectionName} name={sectionName} fields={fields} />
            ))}
          </div>
        )}
      </section>

      <div className="ticks"></div>
      <section id="spacer"></section>
    </>
  )
}

export default App
